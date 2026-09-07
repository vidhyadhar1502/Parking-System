import React from 'react';
import { ParkingSlot, ParkingLocation, SlotStatus } from '../types';
import { X, Car, Zap, Accessibility, ShieldAlert, CheckCircle, Clock } from 'lucide-react';

interface SlotDetailModalProps {
  slot: ParkingSlot;
  location: ParkingLocation;
  onClose: () => void;
  onUpdateStatus: (slotId: string, status: SlotStatus) => void;
  onStartReservation: (slot: ParkingSlot) => void;
}

export const SlotDetailModal: React.FC<SlotDetailModalProps> = ({
  slot,
  location,
  onClose,
  onUpdateStatus,
  onStartReservation,
}) => {
  const isAvailable = slot.status === 'AVAILABLE';
  const isOccupied = slot.status === 'OCCUPIED';
  const isReserved = slot.status === 'RESERVED';
  const isMaintenance = slot.status === 'MAINTENANCE';

  return (
    <div className="fixed inset-0 z-50 bg-[#0F172A]/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl border border-[#E2E8F0] shadow-2xl p-6 w-full max-w-sm relative animate-in fade-in zoom-in-95 duration-200">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-[#94A3B8] hover:text-[#0F172A] p-1 rounded-lg"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center justify-between pb-3 border-b border-[#F1F5F9] mb-4">
          <div>
            <span className="text-[10px] font-mono font-bold text-[#64748B] uppercase">
              Deck 0{slot.floor} • {location.code}
            </span>
            <h3 className="text-xl font-black text-[#0F172A]">
              Bay {slot.slotNumber}
            </h3>
          </div>
          <span
            className={`text-xs px-2.5 py-1 rounded-full font-bold uppercase ${
              isAvailable
                ? 'bg-emerald-100 text-emerald-800'
                : isOccupied
                ? 'bg-rose-100 text-rose-800'
                : isReserved
                ? 'bg-amber-100 text-amber-800'
                : 'bg-slate-200 text-slate-800'
            }`}
          >
            {slot.status}
          </span>
        </div>

        {/* Slot Specifications */}
        <div className="space-y-2.5 p-4 bg-[#F8FAFC] rounded-2xl border border-[#E2E8F0] text-xs mb-5">
          <div className="flex justify-between">
            <span className="text-[#64748B]">Classification:</span>
            <span className="font-bold text-[#0F172A]">{slot.type.replace('_', ' ')}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-[#64748B]">Hourly Tariff:</span>
            <span className="font-bold text-[#38BDF8]">${location.hourlyRate.toFixed(2)}/hr</span>
          </div>
          {slot.occupiedBy && (
            <div className="flex justify-between">
              <span className="text-[#64748B]">Vehicle Tag:</span>
              <span className="font-mono font-bold text-[#0F172A]">{slot.occupiedBy}</span>
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="space-y-2">
          {isAvailable && (
            <>
              <button
                onClick={() => {
                  onClose();
                  onStartReservation(slot);
                }}
                className="w-full py-2.5 bg-[#0F172A] text-white text-xs font-bold rounded-xl hover:bg-[#1E293B] transition-colors shadow-sm cursor-pointer"
              >
                Reserve This Bay Now
              </button>
              <div className="grid grid-cols-2 gap-2">
                <button
                  onClick={() => {
                    onUpdateStatus(slot.id, 'OCCUPIED');
                    onClose();
                  }}
                  className="py-2 bg-rose-50 text-rose-700 hover:bg-rose-100 text-xs font-bold rounded-xl transition-colors cursor-pointer"
                >
                  Simulate Occupy
                </button>
                <button
                  onClick={() => {
                    onUpdateStatus(slot.id, 'MAINTENANCE');
                    onClose();
                  }}
                  className="py-2 bg-slate-100 text-slate-700 hover:bg-slate-200 text-xs font-bold rounded-xl transition-colors cursor-pointer"
                >
                  Maintenance Lock
                </button>
              </div>
            </>
          )}

          {isOccupied && (
            <button
              onClick={() => {
                onUpdateStatus(slot.id, 'AVAILABLE');
                onClose();
              }}
              className="w-full py-2.5 bg-emerald-600 text-white text-xs font-bold rounded-xl hover:bg-emerald-700 transition-colors shadow-sm cursor-pointer flex items-center justify-center gap-1.5"
            >
              <CheckCircle className="w-4 h-4" />
              <span>Free Up Slot (Simulate Exit)</span>
            </button>
          )}

          {isReserved && (
            <button
              onClick={() => {
                onUpdateStatus(slot.id, 'AVAILABLE');
                onClose();
              }}
              className="w-full py-2.5 bg-rose-600 text-white text-xs font-bold rounded-xl hover:bg-rose-700 transition-colors shadow-sm cursor-pointer"
            >
              Cancel Reservation & Free Slot
            </button>
          )}

          {isMaintenance && (
            <button
              onClick={() => {
                onUpdateStatus(slot.id, 'AVAILABLE');
                onClose();
              }}
              className="w-full py-2.5 bg-[#0F172A] text-white text-xs font-bold rounded-xl hover:bg-[#1E293B] transition-colors shadow-sm cursor-pointer"
            >
              Clear Maintenance Lock
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
