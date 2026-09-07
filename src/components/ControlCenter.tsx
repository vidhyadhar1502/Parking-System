import React, { useState } from 'react';
import { ParkingSlot, ActivityLog, ParkingLocation } from '../types';
import { Zap, Accessibility, Car, CheckCircle2, Clock } from 'lucide-react';

interface ControlCenterProps {
  slots: ParkingSlot[];
  locations: ParkingLocation[];
  activityLogs: ActivityLog[];
  onSlotClick: (slot: ParkingSlot) => void;
  onViewAllLogs: () => void;
}

export const ControlCenter: React.FC<ControlCenterProps> = ({
  slots,
  locations,
  activityLogs,
  onSlotClick,
  onViewAllLogs,
}) => {
  const [selectedLevel, setSelectedLevel] = useState<number>(1);
  const [filterType, setFilterType] = useState<string>('ALL');

  // Filter slots for Location 1 (Zone-A) and current selected floor
  const currentFloorSlots = slots.filter(
    (s) => s.locationId === 1 && s.floor === selectedLevel
  );

  const displayedSlots = filterType === 'ALL'
    ? currentFloorSlots
    : currentFloorSlots.filter((s) => s.type === filterType);

  // Group slots by row letter: A, B, C
  const rowASlots = displayedSlots.filter((s) => s.slotNumber.startsWith('A'));
  const rowBSlots = displayedSlots.filter((s) => s.slotNumber.startsWith('B'));
  const rowCSlots = displayedSlots.filter((s) => s.slotNumber.startsWith('C'));

  // Metrics calculation
  const totalSlotsCount = 1250; // Scaled campus capacity
  const liveAvailableCount = slots.filter((s) => s.status === 'AVAILABLE').length + 460;
  const currentBookingsCount = slots.filter((s) => s.status === 'RESERVED' || s.status === 'OCCUPIED').length + 740;
  const dailyRevenue = 4120;

  const getStatusBadge = (status: ParkingSlot['status']) => {
    switch (status) {
      case 'AVAILABLE':
        return { bg: 'bg-[#10B981]', text: 'FREE', border: 'border-white' };
      case 'OCCUPIED':
        return { bg: 'bg-[#EF4444]', text: 'BUSY', border: 'border-white' };
      case 'RESERVED':
        return { bg: 'bg-[#F59E0B]', text: 'RESERV', border: 'border-white' };
      case 'MAINTENANCE':
        return { bg: 'bg-[#64748B]', text: 'LOCK', border: 'border-white' };
    }
  };

  const getTypeIcon = (type: ParkingSlot['type']) => {
    switch (type) {
      case 'EV_CHARGING':
        return <Zap className="w-2.5 h-2.5 opacity-80" />;
      case 'HANDICAPPED':
        return <Accessibility className="w-2.5 h-2.5 opacity-80" />;
      default:
        return null;
    }
  };

  return (
    <div id="control-center-view" className="flex-1 flex flex-col overflow-y-auto">
      {/* 4 Metric Cards from Geometric Balance Theme */}
      <section className="p-8 grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 shrink-0">
        {/* Total Capacity */}
        <div id="metric-total-capacity" className="bg-white p-5 rounded-2xl shadow-sm border border-[#E2E8F0]">
          <p className="text-xs text-[#64748B] uppercase font-bold tracking-wider mb-1">
            Total Capacity
          </p>
          <p className="text-3xl font-black text-[#0F172A]">
            {totalSlotsCount.toLocaleString()}
          </p>
          <div className="h-1.5 w-full bg-[#F1F5F9] mt-3 rounded-full overflow-hidden">
            <div className="h-full w-full bg-[#0F172A]"></div>
          </div>
        </div>

        {/* Live Available */}
        <div id="metric-live-available" className="bg-white p-5 rounded-2xl shadow-sm border border-[#E2E8F0]">
          <p className="text-xs text-[#64748B] uppercase font-bold tracking-wider mb-1">
            Live Available
          </p>
          <p className="text-3xl font-black text-[#10B981]">
            {liveAvailableCount.toLocaleString()}
          </p>
          <div className="h-1.5 w-full bg-[#F1F5F9] mt-3 rounded-full overflow-hidden">
            <div 
              className="h-full bg-[#10B981] transition-all duration-500" 
              style={{ width: `${Math.round((liveAvailableCount / totalSlotsCount) * 100)}%` }}
            ></div>
          </div>
        </div>

        {/* Current Bookings */}
        <div id="metric-current-bookings" className="bg-white p-5 rounded-2xl shadow-sm border border-[#E2E8F0]">
          <p className="text-xs text-[#64748B] uppercase font-bold tracking-wider mb-1">
            Current Bookings
          </p>
          <p className="text-3xl font-black text-[#F59E0B]">
            {currentBookingsCount.toLocaleString()}
          </p>
          <div className="h-1.5 w-full bg-[#F1F5F9] mt-3 rounded-full overflow-hidden">
            <div 
              className="h-full bg-[#F59E0B] transition-all duration-500" 
              style={{ width: `${Math.round((currentBookingsCount / totalSlotsCount) * 100)}%` }}
            ></div>
          </div>
        </div>

        {/* Daily Revenue */}
        <div id="metric-daily-revenue" className="bg-white p-5 rounded-2xl shadow-sm border border-[#E2E8F0]">
          <p className="text-xs text-[#64748B] uppercase font-bold tracking-wider mb-1">
            Daily Revenue
          </p>
          <p className="text-3xl font-black text-[#38BDF8]">
            ${dailyRevenue.toLocaleString()}
          </p>
          <div className="h-1.5 w-full bg-[#F1F5F9] mt-3 rounded-full overflow-hidden">
            <div className="h-full w-[85%] bg-[#38BDF8]"></div>
          </div>
        </div>
      </section>

      {/* Main Interactive Stage: Zone-A Layout + Recent Activity */}
      <section className="px-8 flex flex-col lg:flex-row gap-6 flex-1 pb-8 min-h-[500px]">
        {/* Zone-A Layout Simulation */}
        <div className="flex-[1.5] bg-white rounded-3xl border border-[#E2E8F0] shadow-sm flex flex-col overflow-hidden">
          {/* Header Controls */}
          <div className="p-5 border-b border-[#F1F5F9] flex flex-wrap gap-4 justify-between items-center bg-white">
            <div className="flex items-center gap-3">
              <h3 className="font-bold text-sm uppercase tracking-widest text-[#0F172A]">
                Zone-A Layout Simulation
              </h3>
              <span className="text-[11px] text-[#64748B] bg-[#F1F5F9] px-2 py-0.5 rounded font-mono">
                Central Deck
              </span>
            </div>

            <div className="flex items-center gap-2">
              {/* Floor switcher from Design HTML */}
              <button
                id="btn-level-1"
                onClick={() => setSelectedLevel(1)}
                className={`px-3 py-1 text-[10px] font-bold rounded-full transition-all cursor-pointer ${
                  selectedLevel === 1
                    ? 'bg-[#38BDF8] text-white shadow-sm'
                    : 'bg-[#F1F5F9] text-[#64748B] hover:bg-[#E2E8F0]'
                }`}
              >
                LEVEL 01
              </button>
              <button
                id="btn-level-2"
                onClick={() => setSelectedLevel(2)}
                className={`px-3 py-1 text-[10px] font-bold rounded-full transition-all cursor-pointer ${
                  selectedLevel === 2
                    ? 'bg-[#38BDF8] text-white shadow-sm'
                    : 'bg-[#F1F5F9] text-[#64748B] hover:bg-[#E2E8F0]'
                }`}
              >
                LEVEL 02
              </button>
            </div>
          </div>

          {/* Sub-toolbar: Legend & Quick Filters */}
          <div className="px-5 py-2.5 bg-[#F8FAFC] border-b border-[#F1F5F9] flex flex-wrap items-center justify-between text-xs gap-3">
            <div className="flex items-center gap-4 text-[11px]">
              <span className="flex items-center gap-1.5 font-medium">
                <span className="w-2.5 h-2.5 rounded-xs bg-[#10B981]"></span> Available (Free)
              </span>
              <span className="flex items-center gap-1.5 font-medium">
                <span className="w-2.5 h-2.5 rounded-xs bg-[#EF4444]"></span> Occupied (Busy)
              </span>
              <span className="flex items-center gap-1.5 font-medium">
                <span className="w-2.5 h-2.5 rounded-xs bg-[#F59E0B]"></span> Reserved
              </span>
            </div>

            <div className="flex items-center gap-1">
              <span className="text-[10px] text-[#94A3B8] font-bold uppercase mr-1">Filter:</span>
              {(['ALL', 'STANDARD', 'EV_CHARGING', 'HANDICAPPED'] as const).map((t) => (
                <button
                  key={t}
                  onClick={() => setFilterType(t)}
                  className={`text-[9px] px-2 py-0.5 rounded font-semibold transition-colors cursor-pointer ${
                    filterType === t 
                      ? 'bg-[#0F172A] text-white' 
                      : 'bg-white text-[#64748B] border border-[#E2E8F0] hover:bg-[#F1F5F9]'
                  }`}
                >
                  {t.replace('_', ' ')}
                </button>
              ))}
            </div>
          </div>

          {/* Graphical Blueprint / Canvas with Dot Grid */}
          <div className="flex-1 bg-[#CBD5E1] p-6 relative overflow-y-auto">
            {/* Dot Grid Background Overlay */}
            <div className="absolute inset-0 opacity-25 bg-dot-grid pointer-events-none"></div>

            <div className="relative z-10 flex flex-col justify-between h-full min-h-[380px]">
              {/* Row A: A01 - A08 */}
              <div>
                <div className="text-[9px] font-mono font-bold text-[#475569] mb-1 tracking-wider uppercase">
                  Row A • North Wall
                </div>
                <div className="grid grid-cols-8 gap-3">
                  {rowASlots.map((slot) => {
                    const badge = getStatusBadge(slot.status);
                    return (
                      <button
                        key={slot.id}
                        id={`slot-btn-${slot.slotNumber}`}
                        onClick={() => onSlotClick(slot)}
                        title={`Slot ${slot.slotNumber} (${slot.type}) - Click to inspect or reserve`}
                        className={`h-13 ${badge.bg} rounded-sm border-2 ${badge.border} flex flex-col items-center justify-center text-[10px] text-white font-bold cursor-pointer transition-all hover:scale-105 active:scale-95 shadow-xs`}
                      >
                        <div className="flex items-center gap-0.5">
                          {getTypeIcon(slot.type)}
                          <span>{slot.slotNumber}</span>
                        </div>
                        <span className="font-normal text-[8px] tracking-wide">{badge.text}</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Row B: B01 - B08 */}
              <div className="mt-3">
                <div className="text-[9px] font-mono font-bold text-[#475569] mb-1 tracking-wider uppercase">
                  Row B • Central Bay
                </div>
                <div className="grid grid-cols-8 gap-3">
                  {rowBSlots.map((slot) => {
                    const badge = getStatusBadge(slot.status);
                    return (
                      <button
                        key={slot.id}
                        id={`slot-btn-${slot.slotNumber}`}
                        onClick={() => onSlotClick(slot)}
                        title={`Slot ${slot.slotNumber} (${slot.type}) - Click to inspect or reserve`}
                        className={`h-13 ${badge.bg} rounded-sm border-2 ${badge.border} flex flex-col items-center justify-center text-[10px] text-white font-bold cursor-pointer transition-all hover:scale-105 active:scale-95 shadow-xs`}
                      >
                        <div className="flex items-center gap-0.5">
                          {getTypeIcon(slot.type)}
                          <span>{slot.slotNumber}</span>
                        </div>
                        <span className="font-normal text-[8px] tracking-wide">{badge.text}</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* DRIVEWAY EXIT from Geometric Balance Design */}
              <div className="col-span-8 h-8 flex items-center justify-center text-[10px] font-bold text-[#475569] tracking-[0.5em] my-4 border-y border-[#94A3B8] bg-[#94A3B8]/20 select-none">
                <div className="flex items-center gap-3">
                  <span className="text-[8px] tracking-widest text-[#475569]">◀ ENTRY GATE</span>
                  <span>DRIVEWAY AISLE 01</span>
                  <span className="text-[8px] tracking-widest text-[#475569]">EXIT BARRIER ▶</span>
                </div>
              </div>

              {/* Row C: C01 - C08 */}
              <div>
                <div className="text-[9px] font-mono font-bold text-[#475569] mb-1 tracking-wider uppercase">
                  Row C • South Bay
                </div>
                <div className="grid grid-cols-8 gap-3">
                  {rowCSlots.map((slot) => {
                    const badge = getStatusBadge(slot.status);
                    return (
                      <button
                        key={slot.id}
                        id={`slot-btn-${slot.slotNumber}`}
                        onClick={() => onSlotClick(slot)}
                        title={`Slot ${slot.slotNumber} (${slot.type}) - Click to inspect or reserve`}
                        className={`h-13 ${badge.bg} rounded-sm border-2 ${badge.border} flex flex-col items-center justify-center text-[10px] text-white font-bold cursor-pointer transition-all hover:scale-105 active:scale-95 shadow-xs`}
                      >
                        <div className="flex items-center gap-0.5">
                          {getTypeIcon(slot.type)}
                          <span>{slot.slotNumber}</span>
                        </div>
                        <span className="font-normal text-[8px] tracking-wide">{badge.text}</span>
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Side: Recent Activity from Design HTML */}
        <div className="flex-1 bg-white rounded-3xl border border-[#E2E8F0] shadow-sm flex flex-col min-w-[300px]">
          <div className="p-5 border-b border-[#F1F5F9] flex items-center justify-between">
            <h3 className="font-bold text-sm uppercase tracking-widest text-[#0F172A]">
              Recent Activity
            </h3>
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" title="Live stream active"></span>
          </div>

          <div className="p-5 overflow-auto flex-1">
            <div className="space-y-4">
              {activityLogs.map((log) => (
                <div key={log.id} className="flex items-center justify-between group hover:bg-[#F8FAFC] p-1.5 rounded-lg transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-[#F1F5F9] rounded-lg flex items-center justify-center text-xs font-bold text-[#0F172A] border border-[#E2E8F0]">
                      {log.initials}
                    </div>
                    <div className="flex flex-col">
                      <span className="text-xs font-bold text-[#0F172A]">{log.userName}</span>
                      <span className="text-[10px] text-[#64748B]">
                        {log.action} • {log.detail}
                      </span>
                    </div>
                  </div>
                  <span className="text-[10px] font-mono text-[#64748B] shrink-0 ml-2">
                    {log.time}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div className="p-4 mt-auto border-t border-[#F1F5F9]">
            <button
              id="btn-view-full-logs"
              onClick={onViewAllLogs}
              className="w-full py-2.5 bg-[#0F172A] text-white text-[10px] font-bold uppercase tracking-widest rounded-lg hover:bg-[#1E293B] transition-colors shadow-sm cursor-pointer"
            >
              View Full Logs & Reports
            </button>
          </div>
        </div>
      </section>
    </div>
  );
};
