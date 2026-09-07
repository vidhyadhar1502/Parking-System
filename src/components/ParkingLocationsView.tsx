import React, { useState } from 'react';
import { ParkingLocation } from '../types';
import { MapPin, Navigation, Compass, Layers, DollarSign, Search } from 'lucide-react';

interface ParkingLocationsViewProps {
  locations: ParkingLocation[];
  selectedLocationId: number;
  onSelectLocation: (locationId: number) => void;
  onNavigateToSlots: (locationId: number) => void;
}

export const ParkingLocationsView: React.FC<ParkingLocationsViewProps> = ({
  locations,
  selectedLocationId,
  onSelectLocation,
  onNavigateToSlots,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [mapType, setMapType] = useState<'roadmap' | 'satellite'>('roadmap');

  const filteredLocations = locations.filter(
    (loc) =>
      loc.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      loc.address.toLowerCase().includes(searchQuery.toLowerCase()) ||
      loc.code.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const selectedLoc = locations.find((l) => l.id === selectedLocationId) || locations[0];

  return (
    <div id="locations-view" className="flex-1 flex flex-col p-8 overflow-y-auto">
      {/* Top Banner & Search */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-[#0F172A] tracking-tight">
            Google Maps Parking Discovery
          </h2>
          <p className="text-xs text-[#64748B]">
            Interactive campus geospatial locator with real-time capacity and tariff feeds
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="w-4 h-4 text-[#64748B] absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search by facility or address..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9 pr-4 py-2 bg-white border border-[#E2E8F0] rounded-xl text-xs text-[#0F172A] focus:outline-none focus:border-[#38BDF8] w-64 shadow-xs"
            />
          </div>

          <button
            onClick={() => setMapType(mapType === 'roadmap' ? 'satellite' : 'roadmap')}
            className="px-3 py-2 bg-white border border-[#E2E8F0] rounded-xl text-xs font-semibold text-[#0F172A] hover:bg-[#F8FAFC] flex items-center gap-1.5 shadow-xs"
          >
            <Layers className="w-3.5 h-3.5 text-[#38BDF8]" />
            <span>{mapType === 'roadmap' ? 'Satellite' : 'Roadmap'}</span>
          </button>
        </div>
      </div>

      {/* Main Container: Split Map & Location Details */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 min-h-[500px]">
        {/* Left 2 Cols: Google Maps Simulated Canvas */}
        <div className="lg:col-span-2 bg-white rounded-3xl border border-[#E2E8F0] shadow-sm flex flex-col overflow-hidden relative">
          {/* Map Controls Toolbar */}
          <div className="absolute top-4 left-4 z-20 bg-white/95 backdrop-blur-md px-3 py-1.5 rounded-xl border border-[#E2E8F0] shadow-md flex items-center gap-2">
            <Compass className="w-4 h-4 text-[#38BDF8] animate-spin-slow" />
            <span className="text-[11px] font-bold text-[#0F172A]">
              Google Maps API Canvas
            </span>
            <span className="text-[9px] px-1.5 py-0.5 bg-emerald-100 text-emerald-700 rounded font-mono font-bold">
              LIVE
            </span>
          </div>

          {/* Map Surface */}
          <div
            className={`w-full h-full min-h-[420px] relative overflow-hidden flex items-center justify-center ${
              mapType === 'roadmap' ? 'bg-[#E2E8F0]' : 'bg-[#1E293B]'
            }`}
          >
            {/* Grid styling to emulate map terrain and roads */}
            <div className="absolute inset-0 opacity-40 bg-dot-grid"></div>

            {/* Simulated Road network */}
            <div className="absolute w-full h-12 bg-white/80 -rotate-12 border-y-2 border-slate-300"></div>
            <div className="absolute h-full w-14 bg-white/80 rotate-25 border-x-2 border-slate-300"></div>
            <div className="absolute w-40 h-40 rounded-full border-4 border-white/60 bg-emerald-100/40"></div>

            {/* Simulated Parking Pins */}
            {locations.map((loc) => {
              const isSelected = loc.id === selectedLocationId;
              // Map coordinate offsets for illustration
              const coords: Record<number, { top: string; left: string }> = {
                1: { top: '35%', left: '42%' },
                2: { top: '22%', left: '68%' },
                3: { top: '65%', left: '30%' },
                4: { top: '48%', left: '78%' },
              };
              const pos = coords[loc.id] || { top: '50%', left: '50%' };

              return (
                <div
                  key={loc.id}
                  style={{ top: pos.top, left: pos.left }}
                  className="absolute -translate-x-1/2 -translate-y-1/2 z-30 cursor-pointer transition-transform hover:scale-110"
                  onClick={() => onSelectLocation(loc.id)}
                >
                  <div className="relative group">
                    <div
                      className={`p-2 rounded-xl flex items-center gap-1.5 shadow-lg border-2 transition-all ${
                        isSelected
                          ? 'bg-[#0F172A] text-white border-[#38BDF8] ring-4 ring-[#38BDF8]/20 scale-105'
                          : 'bg-white text-[#0F172A] border-[#CBD5E1] hover:border-[#0F172A]'
                      }`}
                    >
                      <MapPin
                        className={`w-4 h-4 ${isSelected ? 'text-[#38BDF8]' : 'text-rose-500'}`}
                      />
                      <div className="text-left leading-tight">
                        <p className="text-[10px] font-bold truncate max-w-[100px]">{loc.code}</p>
                        <p className="text-[8px] opacity-80">${loc.hourlyRate.toFixed(2)}/hr</p>
                      </div>
                    </div>

                    {/* Info badge popover on hover */}
                    <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 hidden group-hover:block bg-[#0F172A] text-white text-[10px] px-2.5 py-1 rounded-md shadow-lg whitespace-nowrap z-40">
                      {loc.name} • {loc.totalCapacity} Slots
                    </div>
                  </div>
                </div>
              );
            })}

            {/* User current location indicator */}
            <div className="absolute bottom-12 left-16 z-20 flex items-center gap-2 bg-white/90 backdrop-blur-xs px-3 py-1.5 rounded-full shadow-md border border-[#CBD5E1]">
              <div className="w-2.5 h-2.5 rounded-full bg-blue-600 animate-ping"></div>
              <span className="text-[10px] font-bold text-[#0F172A]">Simulated GPS: Main Quad</span>
            </div>
          </div>
        </div>

        {/* Right 1 Col: Location Card & Action Drawer */}
        <div className="bg-white rounded-3xl border border-[#E2E8F0] shadow-sm p-6 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-4 border-b border-[#F1F5F9]">
              <span className="text-[10px] font-mono font-bold text-[#38BDF8] bg-[#0F172A] px-2.5 py-1 rounded-md">
                {selectedLoc.code}
              </span>
              <span className="text-xs text-emerald-600 font-bold flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-emerald-500"></span> Active Station
              </span>
            </div>

            <h3 className="text-lg font-bold text-[#0F172A] mt-4 tracking-tight">
              {selectedLoc.name}
            </h3>
            <p className="text-xs text-[#64748B] mt-1 leading-relaxed">
              {selectedLoc.address}
            </p>

            {/* Coordinates & Capacity Badges */}
            <div className="grid grid-cols-2 gap-3 mt-6">
              <div className="p-3 bg-[#F8FAFC] rounded-xl border border-[#E2E8F0]">
                <span className="text-[10px] text-[#64748B] uppercase font-bold block">Capacity</span>
                <span className="text-lg font-black text-[#0F172A]">{selectedLoc.totalCapacity} Bays</span>
              </div>
              <div className="p-3 bg-[#F8FAFC] rounded-xl border border-[#E2E8F0]">
                <span className="text-[10px] text-[#64748B] uppercase font-bold block">Tariff</span>
                <span className="text-lg font-black text-[#38BDF8]">${selectedLoc.hourlyRate.toFixed(2)}/hr</span>
              </div>
            </div>

            {/* Geographic Meta */}
            <div className="mt-5 p-3.5 bg-[#F1F5F9] rounded-xl border border-[#E2E8F0] text-xs space-y-1.5">
              <div className="flex justify-between text-[#64748B]">
                <span>Floors / Decks:</span>
                <span className="font-bold text-[#0F172A]">{selectedLoc.floors} Levels</span>
              </div>
              <div className="flex justify-between text-[#64748B]">
                <span>GPS Coordinates:</span>
                <span className="font-mono text-[#0F172A] text-[11px]">
                  {selectedLoc.latitude.toFixed(4)}° N, {Math.abs(selectedLoc.longitude).toFixed(4)}° W
                </span>
              </div>
              <div className="flex justify-between text-[#64748B]">
                <span>Estimated Driving Time:</span>
                <span className="font-bold text-emerald-600">3 mins (0.4 miles)</span>
              </div>
            </div>
          </div>

          <div className="mt-6 space-y-2.5">
            <button
              id="btn-navigate-to-slots"
              onClick={() => onNavigateToSlots(selectedLoc.id)}
              className="w-full py-3 bg-[#0F172A] text-white text-xs font-bold uppercase tracking-wider rounded-xl hover:bg-[#1E293B] transition-colors shadow-sm flex items-center justify-center gap-2 cursor-pointer"
            >
              <Navigation className="w-4 h-4 text-[#38BDF8]" />
              <span>Open Slot Layout & Reserve</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
