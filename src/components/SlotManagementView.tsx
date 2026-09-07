import React, { useState } from 'react';
import { ParkingSlot, ParkingLocation, SlotStatus, SlotType } from '../types';
import { Plus, Check, Wrench, ShieldAlert, Sparkles, Filter, Car } from 'lucide-react';

interface SlotManagementViewProps {
  slots: ParkingSlot[];
  locations: ParkingLocation[];
  onUpdateSlotStatus: (slotId: string, newStatus: SlotStatus) => void;
  onAddSlot: (newSlot: Partial<ParkingSlot>) => void;
}

export const SlotManagementView: React.FC<SlotManagementViewProps> = ({
  slots,
  locations,
  onUpdateSlotStatus,
  onAddSlot,
}) => {
  const [selectedLocationId, setSelectedLocationId] = useState<number>(1);
  const [selectedFloor, setSelectedFloor] = useState<number>(1);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [newSlotNumber, setNewSlotNumber] = useState('');
  const [newSlotType, setNewSlotType] = useState<SlotType>('STANDARD');

  const filteredSlots = slots.filter((slot) => {
    const matchLoc = slot.locationId === selectedLocationId;
    const matchFloor = slot.floor === selectedFloor;
    const matchStatus = statusFilter === 'ALL' || slot.status === statusFilter;
    return matchLoc && matchFloor && matchStatus;
  });

  const handleAddSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newSlotNumber.trim()) return;
    onAddSlot({
      locationId: selectedLocationId,
      floor: selectedFloor,
      slotNumber: newSlotNumber.trim().toUpperCase(),
      type: newSlotType,
      status: 'AVAILABLE',
    });
    setNewSlotNumber('');
    setIsAddModalOpen(false);
  };

  return (
    <div id="slot-management-view" className="flex-1 flex flex-col p-8 overflow-y-auto">
      {/* Top Controls */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-[#0F172A] tracking-tight">
            Slot Inventory & Configuration
          </h2>
          <p className="text-xs text-[#64748B]">
            Configure slot availability, assign EV chargers, or toggle maintenance locks
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {/* Location Picker */}
          <select
            value={selectedLocationId}
            onChange={(e) => setSelectedLocationId(Number(e.target.value))}
            className="px-3 py-2 bg-white border border-[#E2E8F0] rounded-xl text-xs font-semibold text-[#0F172A] focus:outline-none focus:border-[#38BDF8] shadow-xs"
          >
            {locations.map((loc) => (
              <option key={loc.id} value={loc.id}>
                {loc.code} - {loc.name}
              </option>
            ))}
          </select>

          {/* Floor Switcher */}
          <div className="flex items-center bg-white border border-[#E2E8F0] p-1 rounded-xl shadow-xs">
            {[1, 2].map((f) => (
              <button
                key={f}
                onClick={() => setSelectedFloor(f)}
                className={`px-3 py-1 text-xs font-bold rounded-lg transition-colors cursor-pointer ${
                  selectedFloor === f
                    ? 'bg-[#0F172A] text-white'
                    : 'text-[#64748B] hover:text-[#0F172A]'
                }`}
              >
                Deck {f}
              </button>
            ))}
          </div>

          <button
            id="btn-add-slot-modal"
            onClick={() => setIsAddModalOpen(true)}
            className="px-3.5 py-2 bg-[#38BDF8] text-[#0F172A] text-xs font-bold rounded-xl hover:bg-[#38BDF8]/90 transition-colors shadow-xs flex items-center gap-1.5 cursor-pointer"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add Slot</span>
          </button>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 mb-6 overflow-x-auto pb-1">
        <Filter className="w-3.5 h-3.5 text-[#64748B] mr-1" />
        {(['ALL', 'AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE'] as const).map((st) => (
          <button
            key={st}
            onClick={() => setStatusFilter(st)}
            className={`text-xs px-3 py-1.5 rounded-full font-semibold transition-colors cursor-pointer whitespace-nowrap ${
              statusFilter === st
                ? 'bg-[#0F172A] text-white shadow-xs'
                : 'bg-white text-[#64748B] border border-[#E2E8F0] hover:bg-[#F8FAFC]'
            }`}
          >
            {st} ({st === 'ALL' ? slots.filter(s => s.locationId === selectedLocationId && s.floor === selectedFloor).length : slots.filter(s => s.locationId === selectedLocationId && s.floor === selectedFloor && s.status === st).length})
          </button>
        ))}
      </div>

      {/* Grid of Slots Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4">
        {filteredSlots.map((slot) => {
          const isAvail = slot.status === 'AVAILABLE';
          const isBusy = slot.status === 'OCCUPIED';
          const isRes = slot.status === 'RESERVED';
          const isMaint = slot.status === 'MAINTENANCE';

          return (
            <div
              key={slot.id}
              className="bg-white rounded-2xl border border-[#E2E8F0] p-4 shadow-xs flex flex-col justify-between hover:border-[#38BDF8] transition-all"
            >
              <div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-black text-[#0F172A]">
                    {slot.slotNumber}
                  </span>
                  <span
                    className={`text-[9px] px-2 py-0.5 rounded-full font-bold uppercase ${
                      isAvail
                        ? 'bg-emerald-100 text-emerald-700'
                        : isBusy
                        ? 'bg-rose-100 text-rose-700'
                        : isRes
                        ? 'bg-amber-100 text-amber-700'
                        : 'bg-slate-200 text-slate-700'
                    }`}
                  >
                    {slot.status}
                  </span>
                </div>

                <div className="mt-2 text-[11px] text-[#64748B] flex items-center gap-1 font-medium">
                  <Car className="w-3 h-3 text-[#38BDF8]" />
                  <span>{slot.type.replace('_', ' ')}</span>
                </div>

                {slot.occupiedBy && (
                  <p className="text-[10px] text-[#0F172A] font-mono mt-1 bg-[#F8FAFC] p-1 rounded border border-[#E2E8F0]">
                    Veh: {slot.occupiedBy}
                  </p>
                )}
              </div>

              {/* Status Action Buttons */}
              <div className="mt-4 pt-3 border-t border-[#F1F5F9] flex items-center justify-between gap-1">
                {isAvail ? (
                  <>
                    <button
                      onClick={() => onUpdateSlotStatus(slot.id, 'OCCUPIED')}
                      className="text-[9px] px-2 py-1 bg-rose-50 text-rose-700 hover:bg-rose-100 rounded font-bold transition-colors cursor-pointer"
                      title="Simulate Vehicle Entry"
                    >
                      Occupy
                    </button>
                    <button
                      onClick={() => onUpdateSlotStatus(slot.id, 'MAINTENANCE')}
                      className="text-[9px] px-2 py-1 bg-slate-100 text-slate-700 hover:bg-slate-200 rounded font-bold transition-colors cursor-pointer"
                      title="Set to Maintenance"
                    >
                      Lock
                    </button>
                  </>
                ) : (
                  <button
                    onClick={() => onUpdateSlotStatus(slot.id, 'AVAILABLE')}
                    className="w-full text-[9px] py-1 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 rounded font-bold transition-colors cursor-pointer flex items-center justify-center gap-1"
                  >
                    <Check className="w-3 h-3" />
                    <span>Free Up Slot</span>
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Add Slot Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 bg-[#0F172A]/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl border border-[#E2E8F0] shadow-xl p-6 w-full max-w-sm">
            <h3 className="text-base font-bold text-[#0F172A] mb-1">
              Add New Parking Slot
            </h3>
            <p className="text-xs text-[#64748B] mb-4">
              Adding to Deck {selectedFloor} of selected facility.
            </p>

            <form onSubmit={handleAddSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#0F172A] mb-1">
                  Slot Identifier (e.g., D01, D02)
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. D01"
                  value={newSlotNumber}
                  onChange={(e) => setNewSlotNumber(e.target.value)}
                  className="w-full px-3 py-2 border border-[#E2E8F0] rounded-xl text-xs font-semibold focus:outline-none focus:border-[#38BDF8]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#0F172A] mb-1">
                  Slot Vehicle Category
                </label>
                <select
                  value={newSlotType}
                  onChange={(e) => setNewSlotType(e.target.value as SlotType)}
                  className="w-full px-3 py-2 border border-[#E2E8F0] rounded-xl text-xs font-semibold focus:outline-none focus:border-[#38BDF8]"
                >
                  <option value="STANDARD">Standard Vehicle</option>
                  <option value="COMPACT">Compact / Hatchback</option>
                  <option value="EV_CHARGING">EV Fast Charging</option>
                  <option value="HANDICAPPED">Accessible (Handicapped)</option>
                </select>
              </div>

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setIsAddModalOpen(false)}
                  className="flex-1 py-2 text-xs font-bold text-[#64748B] hover:bg-[#F1F5F9] rounded-xl cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 py-2 bg-[#0F172A] text-white text-xs font-bold rounded-xl hover:bg-[#1E293B] cursor-pointer"
                >
                  Save Slot
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
