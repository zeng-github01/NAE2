package co.neeve.nae2.mixin.upgrades;

import appeng.parts.automation.UpgradeInventory;
import co.neeve.nae2.common.interfaces.IExtendedUpgradeInventory;
import co.neeve.nae2.common.items.NAEBaseItemUpgrade;
import co.neeve.nae2.common.registries.Upgrades;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = UpgradeInventory.class, remap = false)
public abstract class MixinUpgradeInventory implements IExtendedUpgradeInventory {
	@Unique
	private final HashMap<Upgrades, Integer> nae2$installedUpgrades = new HashMap<>();
	@Shadow
	private boolean cached;

	@Shadow
	private void updateUpgradeInfo() {}

	@Override
	public int getInstalledUpgrades(Upgrades u) {
		if (!this.cached) {
			this.updateUpgradeInfo();
		}

		return nae2$installedUpgrades.getOrDefault(u, 0);
	}

	@Override
	public abstract int getMaxInstalled(Upgrades u);

	@Inject(method = "updateUpgradeInfo", at = @At("HEAD"))
	private void injectUpdateUpgradeInfo(CallbackInfo ci) {
		this.nae2$installedUpgrades.clear();
	}

	@Inject(method = "updateUpgradeInfo", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
		shift = At.Shift.AFTER,
		remap = true,
		ordinal = 0
	))
	private void injectUpdateUpgradeInfoIS(CallbackInfo ci, @Local ItemStack is) {
		var item = is.getItem();
		if (item instanceof NAEBaseItemUpgrade niu) {
			var type = niu.getType(is);
			nae2$installedUpgrades.put(type, nae2$installedUpgrades.getOrDefault(type, 0) + 1);
		}
	}
}