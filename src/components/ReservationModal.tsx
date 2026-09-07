import React, { useState } from 'react';
import { ParkingLocation, ParkingSlot } from '../types';
import { Calendar, Clock, Car, DollarSign, X, CheckCircle } from 'lucide-react';

interface ReservationModalProps {
  locations: ParkingLocation[];
  slots: ParkingSlot[];
  preselectedSlot?: ParkingSlot | null;
  onClose: () => void;
  onConfirmReservation: (data: {
    locationId: number;
    floor: number;
    slotId: string;
    vehicleNumber: string;
    userName: string;
    userEmail: string;
    durationHours: number;
  }) => void;
}

export const ReservationModal: React.FC<ReservationModalProps> = ({
  locations,
  slots,
  preselectedSlot,
  onClose,
  onConfirmReservation,
}) => {
  const [selectedLocationId, setSelectedLocationId] = useState<number>(
    preselectedSlot ? preselectedSlot.locationId : locations[0].id
  );
  const [selectedFloor, setSelectedFloor] = useState<number>(
    preselectedSlot ? preselectedSlot.floor : 1
  );
  const [selectedSlotId, setSelectedSlotId] = useState<string>(
    preselectedSlot ? preselectedSlot.id : ''
  );
  const [vehicleNumber, setVehicleNumber] = useState('CAL-8924');
  const [userName, setUserName] = useState('John Doe');
  const [userEmail, setUserEmail] = useState('john.doe@student.edu');
  const [durationHours, setDurationHours] = useState<number>(2);

  // Available slots for this location and floor
  const availableSlots = slots.filter(
    (s) =>
      s.locationId === selectedLocationId &&
      s.floor === selectedFloor &&
      s.status === 'AVAILABLE'
  );

  const loc = locations.find((l) => l.id === selectedLocationId) || locations[0];
  const totalEstimatedCost = durationHours * loc.hourlyRate;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSlotId) {
      if (availableSlots.length > 0) {
        setSelectedSlotId(availableSlots[0].id);
      } else {
        alert('No available slots on this deck. Please select another deck or facility.');
        return;
      }
    }

    onConfirmReservation({
      locationId: selectedLocationId,
      floor: selectedFloor,
      slotId: selectedSlotId || availableSlots[0].id,
      vehicleNumber: vehicleNumber.trim().toUpperCase(),
      userName: userName.trim(),
      userEmail: userEmail.trim(),
      durationHours,
    });
  };

  return (
    <div className="fixed inset-0 z-50 bg-[#0F172A]/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl border border-[#E2E8F0] shadow-2xl p-6 w-full max-w-md relative animate-in fade-in zoom-in-95 duration-200">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-[#94A3B8] hover:text-[#0F172A] p-1 rounded-lg"
        >
          <X className="w-5 h-5" />
        </button>

        <h3 className="text-lg font-bold text-[#0F172A] tracking-tight mb-1">
          Reserve Parking Slot
        </h3>
        <p className="text-xs text-[#64748B] mb-5">
          Select facility, floor, and bay to lock your spot and get a digital QR ticket.
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Location & Floor */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-bold text-[#0F172A] mb-1">
                Parking Facility
              </label>
              <select
                value={selectedLocationId}
                onChange={(e) => {
                  const val = Number(e.target.value);
                  setSelectedLocationId(val);
                  setSelectedSlotId('');
                }}
                className="w-full px-3 py-2 bg-[#F8FAFC] border border-[#E2E8F0] rounded-xl text-xs font-semibold text-[#0F172A] focus:outline-none focus:border-[#38BDF8]"
              >
                {locations.map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.code} ({l.name.split('-')[0]})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-bold text-[#0F172A] mb-1">
                Deck / Floor
              </label>
              <select
                value={selectedFloor}
                onChange={(e) => {
                  setSelectedFloor(Number(e.target.value));
                  setSelectedSlotId('');
                }}
                className="w-full px-3 py-2 bg-[#F8FAFC] border border-[#E2E8F0] rounded-xl text-xs font-semibold text-[#0F172A] focus:outline-none focus:border-[#38BDF8]"
              >
                {[1, 2].map((f) => (
                  <option key={f} value={f}>
                    Level 0{f}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Slot Selection */}
          <div>
            <label className="block text-[11px] font-bold text-[#0F172A] mb-1">
              Select Available Bay
            </label>
            <select
              value={selectedSlotId}
              onChange={(e) => setSelectedSlotId(e.target.value)}
              className="w-full px-3 py-2 bg-[#F8FAFC] border border-[#E2E8F0] rounded-xl text-xs font-semibold font-mono text-[#0F172A] focus:outline-none focus:border-[#38BDF8]"
            >
              <option value="">-- Choose an Open Bay --</option>
              {availableSlots.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.slotNumber} ({s.type.replace('_', ' ')})
                </option>
              ))}
            </select>
          </div>

          {/* Vehicle Plate & Driver Info */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-bold text-[#0F172A] mb-1">
                License Plate
              </label>
              <input
                type="text"
                required
                value={vehicleNumber}
                onChange={(e) => setVehicleNumber(e.target.value)}
                placeholder="e.g. CAL-8924"
                className="w-full px-3 py-2 bg-[#F8FAFC] border border-[#E2E8F0] rounded-xl text-xs font-mono font-bold uppercase focus:outline-none focus:border-[#38BDF8]"
              />
            </div>
            <div>
              <label className="block text-[11px] font-bold text-[#0F172A] mb-1">
                Duration (Hours)
              </label>
              <input
                type="number"
                min="1"
                max="24"
                value={durationHours}
                onChange={(e) => setDurationHours(Math.max(1, Number(e.target.value)))}
                className="w-full px-3 py-2 bg-[#F8FAFC] border border-[#E2E8F0] rounded-xl text-xs font-bold focus:outline-none focus:border-[#38BDF8]"
              />
            </div>
          </div>

          {/* Rate Summary */}
          <div className="p-3.5 bg-[#F1F5F9] rounded-2xl border border-[#E2E8F0] flex items-center justify-between text-xs">
            <div>
              <span className="text-[#64748B] block text-[10px]">Estimated Total Fee</span>
              <span className="font-bold text-[#0F172A]">
                {durationHours} hrs @ ${loc.hourlyRate.toFixed(2)}/hr
              </span>
            </div>
            <div className="text-right">
              <span className="text-lg font-black text-[#38BDF8]">
                ${totalEstimatedCost.toFixed(2)}
              </span>
            </div>
          </div>

          {/* Submit */}
          <div className="flex gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2.5 text-xs font-bold text-[#64748B] hover:bg-[#F1F5F9] rounded-xl cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="flex-1 py-2.5 bg-[#0F172A] text-white text-xs font-bold rounded-xl hover:bg-[#1E293B] transition-colors shadow-sm cursor-pointer"
            >
              Confirm & Generate QR
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
