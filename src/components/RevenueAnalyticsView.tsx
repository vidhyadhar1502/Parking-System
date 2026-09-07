import React from 'react';
import { Download, TrendingUp, DollarSign, Calendar, BarChart2, ShieldCheck } from 'lucide-react';
import { ParkingLocation } from '../types';

interface RevenueAnalyticsViewProps {
  locations: ParkingLocation[];
}

export const RevenueAnalyticsView: React.FC<RevenueAnalyticsViewProps> = ({ locations }) => {
  const handleExportCSV = () => {
    const csvRows = [
      ['Date', 'Location Code', 'Location Name', 'Vehicles Served', 'Revenue Collected (USD)'],
      ['2026-09-06', 'ZONE-A', 'Campus Block A - Central Deck', '142', '1490.00'],
      ['2026-09-06', 'ZONE-B', 'Engineering & Tech Hub Plaza', '98', '1180.00'],
      ['2026-09-06', 'ZONE-C', 'South Sports Arena Lot', '210', '840.00'],
      ['2026-09-06', 'ZONE-D', 'Medical Sciences Center', '76', '610.00'],
    ];

    const csvContent = 'data:text/csv;charset=utf-8,' + csvRows.map((e) => e.join(',')).join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', 'parksmart_revenue_audit.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div id="analytics-view" className="flex-1 flex flex-col p-8 overflow-y-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-[#0F172A] tracking-tight">
            Financial & Occupancy Analytics
          </h2>
          <p className="text-xs text-[#64748B]">
            Aggregated revenue reporting, parking velocity, and peak-hour distribution
          </p>
        </div>

        <button
          id="btn-export-csv"
          onClick={handleExportCSV}
          className="px-4 py-2 bg-[#0F172A] text-white rounded-xl text-xs font-bold hover:bg-[#1E293B] transition-colors shadow-sm flex items-center gap-2 cursor-pointer"
        >
          <Download className="w-3.5 h-3.5 text-[#38BDF8]" />
          <span>Export Audit (CSV)</span>
        </button>
      </div>

      {/* 3 Overview Stat Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div className="bg-white p-5 rounded-2xl border border-[#E2E8F0] shadow-xs">
          <span className="text-xs text-[#64748B] font-bold uppercase tracking-wider block mb-1">
            Weekly Gross Revenue
          </span>
          <p className="text-2xl font-black text-[#0F172A]">$28,940.00</p>
          <p className="text-[11px] text-emerald-600 font-bold mt-2 flex items-center gap-1">
            <TrendingUp className="w-3 h-3" /> +14.2% vs previous week
          </p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-[#E2E8F0] shadow-xs">
          <span className="text-xs text-[#64748B] font-bold uppercase tracking-wider block mb-1">
            Average Parking Duration
          </span>
          <p className="text-2xl font-black text-[#0F172A]">2.8 Hours</p>
          <p className="text-[11px] text-[#64748B] mt-2">Peak stay time: 11:00 - 15:30</p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-[#E2E8F0] shadow-xs">
          <span className="text-xs text-[#64748B] font-bold uppercase tracking-wider block mb-1">
            Campus Occupancy Peak
          </span>
          <p className="text-2xl font-black text-[#38BDF8]">88.4%</p>
          <p className="text-[11px] text-[#64748B] mt-2">Zone-A Central Deck highest demand</p>
        </div>
      </div>

      {/* Main Analytics Panels */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1">
        {/* Hourly Peak Curve */}
        <div className="bg-white rounded-3xl border border-[#E2E8F0] shadow-sm p-6 flex flex-col">
          <h3 className="font-bold text-sm uppercase tracking-widest text-[#0F172A] mb-1">
            Hourly Vehicle Velocity (Today)
          </h3>
          <p className="text-xs text-[#64748B] mb-6">Arrival rate vs. Departure rate</p>

          <div className="flex-1 flex items-end justify-between gap-2 min-h-[220px] px-2 pt-6 pb-2 border-b border-[#E2E8F0]">
            {[
              { time: '08:00', val: 40 },
              { time: '09:00', val: 78 },
              { time: '10:00', val: 92 },
              { time: '11:00', val: 95 },
              { time: '12:00', val: 86 },
              { time: '13:00', val: 89 },
              { time: '14:00', val: 94 },
              { time: '15:00', val: 72 },
              { time: '16:00', val: 55 },
              { time: '17:00', val: 32 },
            ].map((bar, i) => (
              <div key={bar.time} className="flex-1 flex flex-col items-center gap-1.5 h-full justify-end group">
                <span className="text-[9px] font-mono text-[#64748B] opacity-0 group-hover:opacity-100 transition-opacity">
                  {bar.val}%
                </span>
                <div
                  className="w-full bg-[#0F172A] rounded-t-md hover:bg-[#38BDF8] transition-all cursor-pointer relative"
                  style={{ height: `${bar.val}%` }}
                ></div>
                <span className="text-[9px] font-mono text-[#64748B] mt-1">{bar.time}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Revenue Contribution by Location */}
        <div className="bg-white rounded-3xl border border-[#E2E8F0] shadow-sm p-6 flex flex-col">
          <h3 className="font-bold text-sm uppercase tracking-widest text-[#0F172A] mb-1">
            Revenue Share by Facility
          </h3>
          <p className="text-xs text-[#64748B] mb-6">Contribution to daily $4,120 collection</p>

          <div className="space-y-4 flex-1">
            {locations.map((loc, idx) => {
              const percentages = [36, 29, 20, 15];
              const p = percentages[idx] || 20;
              const rev = Math.round((4120 * p) / 100);

              return (
                <div key={loc.id} className="p-3 bg-[#F8FAFC] rounded-2xl border border-[#E2E8F0]">
                  <div className="flex justify-between items-center mb-1.5">
                    <span className="text-xs font-bold text-[#0F172A]">{loc.name}</span>
                    <span className="text-xs font-mono font-bold text-[#38BDF8]">${rev}</span>
                  </div>
                  <div className="h-2 w-full bg-[#E2E8F0] rounded-full overflow-hidden">
                    <div
                      className="h-full bg-[#0F172A] rounded-full"
                      style={{ width: `${p}%` }}
                    ></div>
                  </div>
                  <div className="flex justify-between text-[10px] text-[#64748B] mt-1">
                    <span>{loc.code}</span>
                    <span>{p}% of daily revenue</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
