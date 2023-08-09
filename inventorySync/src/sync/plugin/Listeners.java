package sync.plugin;

import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredListener;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class Listeners implements Listener {

	private final Main plugin;

	public Player master;

	public Listeners(Main plugin) {
		this.plugin = plugin;

	}

	@EventHandler
	public void onPlayerJoined(PlayerJoinEvent e) {
		if (Bukkit.getOnlinePlayers().size() == 1) {
			master = e.getPlayer();
		} else {
			Player p = e.getPlayer();
			p.getInventory().setStorageContents(master.getInventory().getStorageContents());
			p.getInventory().setArmorContents(master.getInventory().getArmorContents());
			p.getInventory().setItemInOffHand(master.getInventory().getItemInOffHand());

		}

	}

	@EventHandler
	public void dragEvent(InventoryDragEvent e) {
		// e.setCancelled(true);

		Player player = (Player) e.getView().getPlayer();
		Inventory inv = player.getInventory();
		int count = 0;

		if (e.getType() == DragType.SINGLE) {
			// System.out.println(e.getRawSlots());

			for (Integer inter : e.getRawSlots()) {
				e.getView().setItem(inter.intValue(), new ItemStack(e.getOldCursor().getType()));
				count++;
			}

		} else if (e.getType() == DragType.EVEN) {

			for (Integer inter : e.getRawSlots()) {
				e.getView().setItem(inter.intValue(), new ItemStack(e.getOldCursor().getType(),
						e.getOldCursor().getAmount() / e.getRawSlots().size()));
				count += e.getOldCursor().getAmount() / e.getRawSlots().size();
			}

		}

		// System.out.println(count);
		// e.setCursor(new ItemStack(e.getOldCursor().getType(),
		// e.getOldCursor().getAmount()-count));
		// System.out.println(e.getCursor().getAmount());
		changeInven(player);
	}

	@EventHandler
	public void onInventoryChanged(InventoryClickEvent e) {

		if (e.getClickedInventory() == null) {
			return;
		}
		
		
		 if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
	
			if (e.getView().getTopInventory().getType() != InventoryType.CRAFTING
					&& e.getView().getTopInventory() != e.getClickedInventory()) {
				moveShift(e, e.getView().getTopInventory());
	
			} else if (e.getClickedInventory() == e.getView().getTopInventory()) {
				moveShift(e, e.getView().getBottomInventory());
			}
	
		 }
		
		if (e.getSlotType() == SlotType.RESULT) {
			return;
		}

		Player player = (Player) e.getWhoClicked();
		InventoryView view = e.getView();

		e.setCancelled(true);

		ItemStack mouse = e.getCursor().clone();
		ItemStack slot = e.getCurrentItem().clone();

		// Bukkit.broadcastMessage("Mouse: "+ mouse + "\nSlot: " + slot + "\n");

		if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR || e.getAction() == InventoryAction.PICKUP_ALL
				|| e.getAction() == InventoryAction.PLACE_ALL || e.getAction() == InventoryAction.PLACE_SOME) {

			if (mouse.getType() == slot.getType()) {
				slot.setAmount(slot.getAmount() + mouse.getAmount());

				if (slot.getAmount() > slot.getType().getMaxStackSize()) {
					mouse.setAmount(slot.getAmount() - slot.getType().getMaxStackSize());
					slot.setAmount(slot.getType().getMaxStackSize());
				} else {
					mouse.setAmount(0);
				}

				view.setCursor(mouse);
				e.setCurrentItem(slot);

			} else {

				view.setCursor(slot);
				e.setCurrentItem(mouse);
			}

		} else if (e.getAction() == InventoryAction.PLACE_ONE) {
			if (slot.getType() == Material.AIR) {
				slot.setType(mouse.getType());
			} else {

				slot.setAmount(slot.getAmount() + 1);
			}

			mouse.setAmount(mouse.getAmount() - 1);
			view.setCursor(mouse);

			e.setCurrentItem(slot);

		} else if (e.getAction() == InventoryAction.PICKUP_HALF) {
			if (e.getCurrentItem().getAmount() == 1) {
				view.setCursor(e.getCurrentItem());
				e.setCurrentItem(null);
			} else {
				int am = slot.getAmount() % 2;
				slot.setAmount(slot.getAmount() / 2);
				e.setCurrentItem(slot);
				if (am != 0) {
					slot.setAmount(slot.getAmount() + 1);
				}
				view.setCursor(slot);
			}
		} else if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {

			Inventory bottom = e.getView().getBottomInventory();
			Inventory top = e.getView().getTopInventory();

			gatherMats(top, e);
			gatherMats(bottom, e);

		} else if (e.getAction() == InventoryAction.HOTBAR_SWAP) {
			ItemStack hot = player.getInventory().getItem(e.getHotbarButton());

			e.setCurrentItem(hot);
			player.getInventory().setItem(e.getHotbarButton(), slot);

		
		} else {

			return;
		}

		Main.inv.setStorageContents(e.getInventory().getStorageContents());
		for (Player p : Bukkit.getOnlinePlayers()) {

			p.getInventory().setStorageContents(player.getInventory().getStorageContents());
			p.getInventory().setArmorContents(player.getInventory().getArmorContents());
			p.getInventory().setItemInOffHand(player.getInventory().getItemInOffHand());

		}
	}

	public void moveShift(InventoryClickEvent e, Inventory inv) {

		ItemStack[] contents = inv.getContents();

		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item == null) {
				contents[i] = e.getCurrentItem();
				e.setCurrentItem(new ItemStack(Material.AIR));
				break;
			} else if (item.getType() == e.getCurrentItem().getType() && item.getAmount() < 64) {

				if (e.getCurrentItem().getAmount() + contents[i].getAmount() <= 64) {
					contents[i] = new ItemStack(e.getCurrentItem().getType(),
							e.getCurrentItem().getAmount() + contents[i].getAmount());
					e.setCurrentItem(new ItemStack(Material.AIR));
					break;
				} else {
					e.getCurrentItem().setAmount((e.getCurrentItem().getAmount() + contents[i].getAmount()) % 64);
					contents[i] = new ItemStack(e.getCurrentItem().getType(), 64);
				}

			}
		}
		inv.setContents(contents);
	}

	public void gatherMats(Inventory inv, InventoryClickEvent e) {
		for (ItemStack item : inv.getContents()) {
			if (item != null && item.getType() == e.getCursor().getType() && e.getCursor() != item) {
				if (e.getCursor().getAmount() + item.getAmount() > e.getCursor().getType().getMaxStackSize()) {

					if (item.getAmount() - (64 - e.getCursor().getAmount()) > 0) {

						item.setAmount(item.getAmount() - (64 - e.getCursor().getAmount()));
						e.getCursor().setAmount(64);
					}

				} else {
					System.out.println("NOPERS");
					e.getView().setCursor(
							new ItemStack(e.getCursor().getType(), e.getCursor().getAmount() + item.getAmount()));
					item.setAmount(0);
				}
			}
		}
	}

	@EventHandler
	public void playerDamage(EntityDamageEvent e) {
		if (e.getEntityType() == EntityType.PLAYER) {

			Player player = (Player) e.getEntity();

			ItemStack[] armor = player.getInventory().getArmorContents();
			
			
			
			if (player.getInventory().getItemInOffHand() != null) {
				ItemStack item = ((Player)e.getEntity()).getInventory().getItemInOffHand();
				
				
				
				Damageable dm = (Damageable) item.getItemMeta();
				
				
				
				int durability = item.getType().getMaxDurability() - dm.getDamage();
				
				
				
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p != (Player) e.getEntity()) {
						Damageable dam = ((Damageable) item.getItemMeta());
						if (!(dam.getDamage() > item.getType().getMaxDurability())) {
							
							dam.setDamage(dm.getDamage() + 1);
							item.setItemMeta(dam);
						} else {
							item.setAmount(0);
						}
						
					}
				}
				
			}
			
			changeInven((Player) e.getEntity());
			
			
			
			for (ItemStack armorPiece : armor) {
				
				if (armorPiece == null) {
					return;
				}
				
				
				
				Damageable dm1 = (Damageable) armorPiece.getItemMeta();
				
				
				
				int durability1 = armorPiece.getType().getMaxDurability() - dm1.getDamage();
				

					
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p != (Player) e.getEntity()) {
						Damageable dam = ((Damageable) armorPiece.getItemMeta());
						if (!(dam.getDamage() > armorPiece.getType().getMaxDurability())) {
							
							dam.setDamage(dm1.getDamage() + 1);
							armorPiece.setItemMeta(dam);
						} else {
							armorPiece.setAmount(0);
						}
						
					}
				}
					
				changeInven((Player) e.getEntity());
				
			}
			
			
			
			
			
			
			

		}

	}

	@EventHandler
	public void itemDropped(PlayerDropItemEvent e) {

		changeInven(e.getPlayer());
	}

	@EventHandler
	public void used(PlayerInteractEvent e) {
		if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)
				&& e.getItem() != null) {
			// e.setCancelled(true);
			
			
			if (e.getItem().getItemMeta() instanceof Damageable) {
				return;
			}
			

			int slot = e.getPlayer().getInventory().getHeldItemSlot();

			if (e.getItem().getItemMeta() instanceof ArmorMeta) {
				e.setCancelled(true);

				ItemStack oldItem = null;

				if (e.getItem().getType().toString().contains("HELMET")) {
					oldItem = e.getPlayer().getInventory().getHelmet();

					e.getPlayer().getInventory().setHelmet(e.getItem());
				} else if (e.getItem().getType().toString().contains("CHESTPLATE")) {
					oldItem = e.getPlayer().getInventory().getChestplate();
					e.getPlayer().getInventory().setChestplate(e.getItem());

				} else if (e.getItem().getType().toString().contains("LEGGINGS")) {
					oldItem = e.getPlayer().getInventory().getLeggings();

					e.getPlayer().getInventory().setLeggings(e.getItem());

				} else if (e.getItem().getType().toString().contains("BOOTS")) {
					oldItem = e.getPlayer().getInventory().getBoots();

					e.getPlayer().getInventory().setBoots(e.getItem());

				}
				e.getPlayer().getInventory().setItemInMainHand(oldItem);
				e.getPlayer().playSound(e.getPlayer(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.5F, 1F);
			}

			if (e.getItem().getAmount() != 1) { // is not 1
				e.getItem().setAmount(e.getItem().getAmount() - 1);
				changeInven(e.getPlayer());
				e.getItem().setAmount(e.getItem().getAmount() + 1);
			} else { // is 1

				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p != e.getPlayer()) {
						p.getInventory().clear(slot);

					}
				}

				// changeInven(e.getPlayer());
			}
		}
	}

	@EventHandler
	public void toolUsed(PlayerItemDamageEvent e) {
		changeInven(e.getPlayer());
	}

	@EventHandler
	public void toolMended(PlayerItemMendEvent e) {
		changeInven(e.getPlayer());
	}

	@EventHandler
	public void pickupArrow(PlayerPickupArrowEvent e) {
		changeInven(e.getPlayer());
	}

	@EventHandler
	public void itemBreak(PlayerItemBreakEvent e) {
		changeInven(e.getPlayer());
	}

	@EventHandler
	public void swapHand(PlayerSwapHandItemsEvent e) {

		ItemStack main = e.getPlayer().getInventory().getItemInMainHand();
		ItemStack off = e.getPlayer().getInventory().getItemInOffHand();

		e.getPlayer().getInventory().setItemInMainHand(off);
		e.getPlayer().getInventory().setItemInOffHand(main);

		e.setCancelled(true);

		changeInven(e.getPlayer());
	}

	@EventHandler
	public void onPickup(EntityPickupItemEvent e) {

		if (e.getEntityType() == EntityType.PLAYER) {

			Player player = Bukkit.getPlayer(e.getEntity().getUniqueId());
			e.setCancelled(true);
			e.getItem().remove();
			player.playSound(e.getItem().getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1F);
			player.getInventory().addItem(e.getItem().getItemStack());

			changeInven(player);

		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		e.getEntity().getInventory().clear();
		changeInven(e.getEntity());
	}

	@EventHandler
	public void onBlock(BlockBreakEvent e) {
		if (!(e.getPlayer().getInventory().getItemInMainHand() instanceof Damageable)) {
			return;
		}
		Damageable dm = (Damageable) e.getPlayer().getInventory().getItemInMainHand().getItemMeta();



		int durability = e.getPlayer().getInventory().getItemInMainHand().getType().getMaxDurability() - dm.getDamage();

		if (durability != 1) { // is not 1
			dm.setDamage(dm.getDamage() + 1);
			e.getPlayer().getInventory().getItemInMainHand().setItemMeta(dm);
			changeInven(e.getPlayer());
			dm.setDamage(dm.getDamage() - 1);
			e.getPlayer().getInventory().getItemInMainHand().setItemMeta(dm);
		} else { // is 1

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p != e.getPlayer()) {
					Damageable dam = ((Damageable) p.getInventory()
							.getItem(e.getPlayer().getInventory().getHeldItemSlot()).getItemMeta());
					dam.setDamage(dm.getDamage() + 1);
					e.getPlayer().getInventory().getItemInMainHand().setItemMeta(dam);

				}
			}

			changeInven(e.getPlayer());
		}

	}

	@EventHandler
	public void slotSwitch(PlayerItemHeldEvent e) {
	
		
//		for (RegisteredListener rL : e.getHandlerList().getRegisteredListeners()) {
//			e.getHandlerList().unregister(rL);
//		}
		
		
		if (Main.hotbar) {
//			e.setCancelled(true);
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p != e.getPlayer()) {
					Bukkit.broadcastMessage(e.getPlayer() + "");
					p.getInventory().setHeldItemSlot(e.getNewSlot());
				}
			}
		}
	}

	@EventHandler
	public void onMouseClick(PlayerInteractEvent e) {
		if (e.getMaterial() == Material.BEDROCK) {
			e.getPlayer().getInventory().setHeldItemSlot(1);
			master = e.getPlayer();
			e.getPlayer().openInventory(Main.inv);
		}
	}

	@EventHandler
	public void onMasterLeave(PlayerQuitEvent e) {
		if (e.getPlayer() == master && e.getPlayer().getServer().getOnlinePlayers().size() != 0) {
			master = e.getPlayer().getServer().getOnlinePlayers().iterator().next();
		}
	}

	public void changeInven(Player player) {
		Main.inv.setStorageContents(player.getInventory().getStorageContents());

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (player != p) {

				p.getInventory().setStorageContents(player.getInventory().getStorageContents());
				p.getInventory().setArmorContents(player.getInventory().getArmorContents());
				p.getInventory().setItemInOffHand(player.getInventory().getItemInOffHand());
			}

		}
	}
}