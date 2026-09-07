import React from 'react';
import { Reservation } from '../types';
import { QrCode, Download, CheckCircle2, ArrowRight, X } from 'lucide-react';

interface QRCodeModalProps {
  reservation: Reservation;
  onClose: () => void;
  onGoToGate: (token: string) => void;
}

export const QRCodeModal: React.FC<QRCodeModalProps> = ({
  reservation,
  onClose,
  onGoToGate,
}) => {
  return (
    <div className="fixed inset-0 z-50 bg-[#0F172A]/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl border border-[#E2E8F0] shadow-2xl p-6 w-full max-w-sm flex flex-col items-center relative animate-in fade-in zoom-in-95 duration-200">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-[#94A3B8] hover:text-[#0F172A] p-1 rounded-lg"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="w-10 h-10 bg-emerald-100 rounded-2xl flex items-center justify-center text-emerald-600 mb-3">
          <CheckCircle2 className="w-6 h-6" />
        </div>

        <h3 className="text-base font-bold text-[#0F172A] tracking-tight">
          Booking Confirmed!
        </h3>
        <p className="text-xs text-[#64748B] text-center mb-5">
          Scan this digital QR pass at the entrance terminal barrier.
        </p>

        {/* Realistic Styled QR Matrix Card */}
        <div className="p-4 bg-white rounded-2xl border-2 border-dashed border-[#CBD5E1] shadow-inner flex flex-col items-center">
          <div className="w-44 h-44 bg-white p-2 rounded-xl border border-[#E2E8F0] flex items-center justify-center relative shadow-xs">
            {/* SVG QR Code Simulation with crisp squares */}
            <svg
              className="w-full h-full text-[#0F172A]"
              viewBox="0 0 100 100"
              fill="currentColor"
            >
              {/* Corner Finder 1 */}
              <rect x="5" y="5" width="25" height="25" rx="3" fill="#0F172A" />
              <rect x="10" y="10" width="15" height="15" rx="2" fill="#FFFFFF" />
              <rect x="13" y="13" width="9" height="9" rx="1" fill="#0F172A" />

              {/* Corner Finder 2 */}
              <rect x="70" y="5" width="25" height="25" rx="3" fill="#0F172A" />
              <rect x="75" y="10" width="15" height="15" rx="2" fill="#FFFFFF" />
              <rect x="78" y="13" width="9" height="9" rx="1" fill="#0F172A" />

              {/* Corner Finder 3 */}
              <rect x="5" y="70" width="25" height="25" rx="3" fill="#0F172A" />
              <rect x="10" y="75" width="15" height="15" rx="2" fill="#FFFFFF" />
              <rect x="13" y="78" width="9" height="9" rx="1" fill="#0F172A" />

              {/* Matrix Data Noise representing encrypted token */}
              <rect x="35" y="10" width="5" height="5" />
              <rect x="45" y="10" width="5" height="5" />
              <rect x="55" y="10" width="5" height="5" />
              <rect x="40" y="20" width="5" height="5" />
              <rect x="50" y="20" width="5" height="5" />
              <rect x="60" y="20" width="5" height="5" />
              <rect x="35" y="35" width="6" height="6" />
              <rect x="45" y="35" width="6" height="6" />
              <rect x="55" y="35" width="6" height="6" />
              <rect x="65" y="35" width="6" height="6" />
              <rect x="15" y="40" width="5" height="5" />
              <rect x="25" y="45" width="5" height="5" />
              <rect x="75" y="40" width="5" height="5" />
              <rect x="85" y="45" width="5" height="5" />
              <rect x="35" y="50" width="8" height="8" fill="#38BDF8" />
              <rect x="50" y="50" width="5" height="5" />
              <rect x="60" y="50" width="5" height="5" />
              <rect x="35" y="65" width="5" height="5" />
              <rect x="45" y="65" width="5" height="5" />
              <rect x="55" y="65" width="5" height="5" />
              <rect x="40" y="75" width="6" height="6" />
              <rect x="50" y="75" width="6" height="6" />
              <rect x="65" y="75" width="6" height="6" />
              <rect x="75" y="75" width="6" height="6" />
              <rect x="85" y="85" width="5" height="5" />
            </svg>
          </div>

          <div className="mt-3 text-center">
            <span className="font-mono text-xs font-black text-[#0F172A] tracking-wider">
              {reservation.token}
            </span>
            <p className="text-[10px] text-[#64748B]">
              Slot: {reservation.slotId} • Deck {reservation.floor}
            </p>
          </div>
        </div>

        {/* Ticket Metadata */}
        <div className="w-full mt-4 p-3 bg-[#F8FAFC] rounded-xl border border-[#E2E8F0] text-xs space-y-1">
          <div className="flex justify-between text-[#64748B]">
            <span>Vehicle Plate:</span>
            <span className="font-mono font-bold text-[#0F172A]">{reservation.vehicleNumber}</span>
          </div>
          <div className="flex justify-between text-[#64748B]">
            <span>Location:</span>
            <span className="font-bold text-[#0F172A] truncate max-w-[170px]">{reservation.locationName}</span>
          </div>
          <div className="flex justify-between text-[#64748B]">
            <span>Estimated Fee:</span>
            <span className="font-bold text-[#38BDF8]">${reservation.totalAmount.toFixed(2)}</span>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="w-full mt-5 space-y-2">
          <button
            onClick={() => onGoToGate(reservation.token)}
            className="w-full py-2.5 bg-[#0F172A] text-white text-xs font-bold uppercase tracking-wider rounded-xl hover:bg-[#1E293B] transition-colors shadow-sm flex items-center justify-center gap-1.5 cursor-pointer"
          >
            <span>Proceed to Gate Simulator</span>
            <ArrowRight className="w-3.5 h-3.5 text-[#38BDF8]" />
          </button>
          <button
            onClick={onClose}
            className="w-full py-2 text-xs font-bold text-[#64748B] hover:bg-[#F1F5F9] rounded-xl transition-colors cursor-pointer"
          >
            Done / Close Pass
          </button>
        </div>
      </div>
    </div>
  );
};
