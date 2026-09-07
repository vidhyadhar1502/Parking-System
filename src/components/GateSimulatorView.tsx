import React, { useState } from 'react';
import { Reservation, ParkingSlot } from '../types';
import { QrCode, Scan, ArrowRight, ShieldCheck, CheckCircle2, DollarSign, Clock, Receipt, Sparkles } from 'lucide-react';

interface GateSimulatorViewProps {
  reservations: Reservation[];
  slots: ParkingSlot[];
  onCheckIn: (token: string) => { success: boolean; message: string; reservation?: Reservation };
  onCheckOut: (token: string) => { success: boolean; message: string; fee?: number; hours?: number };
}

export const GateSimulatorView: React.FC<GateSimulatorViewProps> = ({
  reservations,
  slots,
  onCheckIn,
  onCheckOut,
}) => {
  const [inputToken, setInputToken] = useState('');
  const [mode, setMode] = useState<'CHECK_IN' | 'CHECK_OUT'>('CHECK_IN');
  const [barrierState, setBarrierState] = useState<'CLOSED' | 'OPENING' | 'OPEN'>('CLOSED');
  const [statusMessage, setStatusMessage] = useState<{ type: 'success' | 'error' | 'info'; text: string } | null>({
    type: 'info',
    text: 'Awaiting token entry or virtual QR scanner read.',
  });
  const [lastReceipt, setLastReceipt] = useState<{
    token: string;
    vehicle: string;
    hours: number;
    amount: number;
    slot: string;
  } | null>(null);

  const handleProcess = () => {
    if (!inputToken.trim()) {
      setStatusMessage({ type: 'error', text: 'Please enter or select a valid reservation token.' });
      return;
    }

    const token = inputToken.trim();

    if (mode === 'CHECK_IN') {
      const result = onCheckIn(token);
      if (result.success) {
        setStatusMessage({ type: 'success', text: result.message });
        setBarrierState('OPENING');
        setTimeout(() => setBarrierState('OPEN'), 600);
        setTimeout(() => setBarrierState('CLOSED'), 3500);
      } else {
        setStatusMessage({ type: 'error', text: result.message });
      }
    } else {
      const result = onCheckOut(token);
      if (result.success) {
        setStatusMessage({ type: 'success', text: result.message });
        const res = reservations.find((r) => r.token.toUpperCase() === token.toUpperCase());
        setLastReceipt({
          token,
          vehicle: res?.vehicleNumber || 'CAL-8924',
          hours: result.hours || 2.5,
          amount: result.fee || 8.75,
          slot: res?.slotId || 'A-03',
        });
        setBarrierState('OPENING');
        setTimeout(() => setBarrierState('OPEN'), 600);
        setTimeout(() => setBarrierState('CLOSED'), 3500);
      } else {
        setStatusMessage({ type: 'error', text: result.message });
      }
    }
  };

  return (
    <div id="gate-simulator-view" className="flex-1 flex flex-col p-8 overflow-y-auto">
      {/* Title */}
      <div className="mb-6">
        <h2 className="text-xl font-bold text-[#0F172A] tracking-tight">
          Simulated Gate & Optical Barrier Terminal
        </h2>
        <p className="text-xs text-[#64748B]">
          College demonstration terminal for QR badge validation, automated time-stamping, and checkout billing
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left 7 Cols: Gate Control Console */}
        <div className="lg:col-span-7 bg-white rounded-3xl border border-[#E2E8F0] shadow-sm p-6 flex flex-col justify-between">
          <div>
            {/* Mode Switcher */}
            <div className="flex items-center justify-between pb-4 border-b border-[#F1F5F9] mb-6">
              <div className="flex items-center gap-2">
                <button
                  onClick={() => {
                    setMode('CHECK_IN');
                    setStatusMessage(null);
                  }}
                  className={`px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                    mode === 'CHECK_IN'
                      ? 'bg-[#0F172A] text-white shadow-sm'
                      : 'bg-[#F1F5F9] text-[#64748B] hover:bg-[#E2E8F0]'
                  }`}
                >
                  Entry Gate (Check-In)
                </button>
                <button
                  onClick={() => {
                    setMode('CHECK_OUT');
                    setStatusMessage(null);
                  }}
                  className={`px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                    mode === 'CHECK_OUT'
                      ? 'bg-[#0F172A] text-white shadow-sm'
                      : 'bg-[#F1F5F9] text-[#64748B] hover:bg-[#E2E8F0]'
                  }`}
                >
                  Exit Gate (Check-Out & Fee)
                </button>
              </div>

              <div className="flex items-center gap-1.5 text-xs text-[#64748B] font-mono">
                <ShieldCheck className="w-4 h-4 text-[#38BDF8]" />
                <span>Barrier ID: GATE-01A</span>
              </div>
            </div>

            {/* Simulated Barrier Visual Animation */}
            <div className="p-6 bg-[#0F172A] rounded-2xl text-white mb-6 relative overflow-hidden flex flex-col items-center justify-center min-h-[160px]">
              <div className="absolute top-3 left-4 text-[10px] font-mono text-[#94A3B8] uppercase">
                Hardware Simulation: Boom Barrier
              </div>

              {/* Barrier Pole Graphic */}
              <div className="w-full max-w-sm flex items-center justify-between mt-2">
                {/* Pillar */}
                <div className="w-8 h-20 bg-amber-500 rounded-sm border-2 border-white flex flex-col justify-end p-1">
                  <div className="w-full h-2 bg-black/40 rounded-xs mb-1"></div>
                  <div className="w-full h-2 bg-black/40 rounded-xs mb-1"></div>
                  <div className="w-full h-2 bg-black/40 rounded-xs"></div>
                </div>

                {/* Barrier Arm */}
                <div
                  className={`flex-1 h-3 rounded-r-md border border-white mx-2 transition-transform duration-700 origin-left relative ${
                    barrierState === 'OPEN' || barrierState === 'OPENING'
                      ? '-rotate-60 -translate-y-4'
                      : 'rotate-0'
                  }`}
                  style={{
                    backgroundImage: 'repeating-linear-gradient(45deg, #EF4444, #EF4444 12px, #FFFFFF 12px, #FFFFFF 24px)',
                  }}
                ></div>

                {/* Right Sensor Pod */}
                <div className="w-6 h-12 bg-slate-700 rounded-sm border border-slate-500 flex items-center justify-center">
                  <div
                    className={`w-2.5 h-2.5 rounded-full ${
                      barrierState === 'OPEN' ? 'bg-emerald-400' : 'bg-rose-500'
                    }`}
                  ></div>
                </div>
              </div>

              <div className="mt-4 flex items-center gap-3">
                <span
                  className={`text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider font-mono ${
                    barrierState === 'OPEN'
                      ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                      : 'bg-rose-500/20 text-rose-300 border border-rose-500/40'
                  }`}
                >
                  Barrier: {barrierState === 'OPEN' ? 'OPEN - DRIVE THROUGH' : 'LOCKED - STOP'}
                </span>
              </div>
            </div>

            {/* Input & Scanner Form */}
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#0F172A] mb-1.5">
                  Scan QR Badge or Enter Reservation Code
                </label>
                <div className="flex gap-2">
                  <div className="relative flex-1">
                    <QrCode className="w-4 h-4 text-[#64748B] absolute left-3 top-3" />
                    <input
                      type="text"
                      placeholder="e.g. PK-44129 or PK-98214"
                      value={inputToken}
                      onChange={(e) => setInputToken(e.target.value)}
                      className="w-full pl-9 pr-4 py-2.5 bg-[#F8FAFC] border border-[#E2E8F0] rounded-xl text-xs font-mono font-bold text-[#0F172A] uppercase focus:outline-none focus:border-[#38BDF8]"
                    />
                  </div>
                  <button
                    onClick={handleProcess}
                    className="px-5 py-2.5 bg-[#0F172A] text-white text-xs font-bold uppercase rounded-xl hover:bg-[#1E293B] transition-colors shadow-xs cursor-pointer flex items-center gap-1.5"
                  >
                    <span>{mode === 'CHECK_IN' ? 'Check In' : 'Check Out'}</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              {/* Quick Select Tokens from DB for Easy Viva Demo */}
              <div>
                <span className="text-[10px] text-[#64748B] font-bold uppercase tracking-wider block mb-1.5">
                  Demo Quick-Select Active Reservations:
                </span>
                <div className="flex flex-wrap gap-2">
                  {reservations.map((res) => (
                    <button
                      key={res.id}
                      onClick={() => {
                        setInputToken(res.token);
                        setMode(res.status === 'CONFIRMED' ? 'CHECK_IN' : 'CHECK_OUT');
                      }}
                      className="px-2.5 py-1 bg-[#F1F5F9] hover:bg-[#E2E8F0] text-[#0F172A] text-[11px] font-mono rounded-lg border border-[#CBD5E1] transition-colors cursor-pointer flex items-center gap-1"
                    >
                      <span className="font-bold">{res.token}</span>
                      <span className="text-[9px] text-[#64748B]">({res.status})</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Status Notice */}
              {statusMessage && (
                <div
                  className={`p-3.5 rounded-xl border text-xs font-medium ${
                    statusMessage.type === 'success'
                      ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
                      : statusMessage.type === 'error'
                      ? 'bg-rose-50 text-rose-800 border-rose-200'
                      : 'bg-blue-50 text-blue-800 border-blue-200'
                  }`}
                >
                  {statusMessage.text}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right 5 Cols: Automated Fee Calculation & Receipt Card */}
        <div className="lg:col-span-5 bg-white rounded-3xl border border-[#E2E8F0] shadow-sm p-6 flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 pb-4 border-b border-[#F1F5F9]">
              <Receipt className="w-5 h-5 text-[#38BDF8]" />
              <h3 className="font-bold text-sm uppercase tracking-widest text-[#0F172A]">
                Parking Invoice & Receipt
              </h3>
            </div>

            {lastReceipt ? (
              <div className="mt-5 space-y-4">
                <div className="p-4 bg-[#F8FAFC] rounded-2xl border border-[#E2E8F0] font-mono text-xs space-y-2">
                  <div className="flex justify-between">
                    <span className="text-[#64748B]">Token:</span>
                    <span className="font-bold text-[#0F172A]">{lastReceipt.token}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-[#64748B]">Vehicle Number:</span>
                    <span className="font-bold text-[#0F172A]">{lastReceipt.vehicle}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-[#64748B]">Slot Allocated:</span>
                    <span className="font-bold text-emerald-600">{lastReceipt.slot}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-[#64748B]">Time Parked:</span>
                    <span className="font-bold text-[#0F172A]">{lastReceipt.hours} hrs</span>
                  </div>
                  <div className="h-px bg-[#E2E8F0] my-2" />
                  <div className="flex justify-between text-sm">
                    <span className="font-bold text-[#0F172A]">Total Collected:</span>
                    <span className="font-black text-emerald-600">
                      ${lastReceipt.amount.toFixed(2)}
                    </span>
                  </div>
                </div>

                <div className="p-3 bg-emerald-50 rounded-xl border border-emerald-200 flex items-center gap-2 text-xs text-emerald-800">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                  <span>Transaction logged into MySQL `payments` table.</span>
                </div>
              </div>
            ) : (
              <div className="py-12 flex flex-col items-center justify-center text-center">
                <div className="w-12 h-12 bg-[#F1F5F9] rounded-2xl flex items-center justify-center mb-3">
                  <DollarSign className="w-6 h-6 text-[#94A3B8]" />
                </div>
                <p className="text-xs font-bold text-[#0F172A]">No Active Checkout</p>
                <p className="text-[11px] text-[#64748B] max-w-xs mt-1">
                  Perform an exit check-out operation on the left to calculate duration fees and print receipts.
                </p>
              </div>
            )}
          </div>

          <div className="p-3 bg-[#F1F5F9] rounded-xl border border-[#E2E8F0] text-[10px] text-[#64748B] mt-4">
            <span className="font-bold text-[#0F172A] block mb-0.5">Billing Formula:</span>
            Ceiling(Parked Hours) × Hourly Tariff ($3.50) + Grace Period (15 mins waived).
          </div>
        </div>
      </div>
    </div>
  );
};
