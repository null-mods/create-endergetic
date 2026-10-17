package net.nullmods.createendergy.content.wire;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nullmods.createendergy.content.EndergyGoggles;
import net.nullmods.createendergy.content.EndergySource;
import net.nullmods.createendergy.content.accumulator.AccumulatorBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class InsulatorBlockEntity extends BlockEntity implements IHaveGoggleInformation {
    public static final int MAX_LINKS = 4;
    public static final double MAX_LENGTH = 16;

    public record Link(BlockPos other, WireType type, boolean outgoing) {
    }

    private final List<Link> links = new ArrayList<>();

    public InsulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public List<Link> getLinks() {
        return links;
    }

    public boolean isLinkedTo(BlockPos other) {
        return links.stream().anyMatch(link -> link.other().equals(other));
    }

    public boolean isFull() {
        return links.size() >= MAX_LINKS;
    }

    public Vec3 getAttachPoint(InsulatorBlock.Neck neck) {
        return InsulatorBlock.attachPoint(worldPosition, getBlockState(), neck);
    }

    public EndergySource getSource() {
        BlockState state = getBlockState();
        if (!state.hasProperty(InsulatorBlock.FACING))
            return null;
        BlockEntity mount = level.getBlockEntity(worldPosition.relative(state.getValue(InsulatorBlock.FACING).getOpposite()));
        EndergySource source = mount instanceof AccumulatorBlockEntity part ? part.getControllerBE()
                : mount instanceof EndergySource other ? other : null;
        return source != null && source.getVoltage() > 0 ? source : null;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        EndergyGoggles.header(tooltip);
        EndergyGoggles.voltage(tooltip, EndergyNetwork.voltageAt(level, worldPosition));
        return true;
    }

    public void addLink(BlockPos other, WireType type, boolean outgoing) {
        links.add(new Link(other, type, outgoing));
        changed();
    }

    public void removeLink(BlockPos other) {
        if (links.removeIf(link -> link.other().equals(other)))
            changed();
    }

    public void dropWires() {
        for (Link link : List.copyOf(links)) {
            if (level.getBlockEntity(link.other()) instanceof InsulatorBlockEntity end)
                end.removeLink(worldPosition);
            Containers.dropItemStack(level, worldPosition.getX() + .5, worldPosition.getY() + .5,
                    worldPosition.getZ() + .5,
                    new ItemStack(link.type().item()));
        }
        links.clear();
    }

    private void changed() {
        setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (Link link : links) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", link.other().asLong());
            entry.putString("Type", link.type().getSerializedName());
            entry.putBoolean("Outgoing", link.outgoing());
            list.add(entry);
        }
        tag.put("Links", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        links.clear();
        for (Tag t : tag.getList("Links", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            links.add(new Link(BlockPos.of(entry.getLong("Pos")), WireType.byName(entry.getString("Type")),
                    entry.getBoolean("Outgoing")));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
