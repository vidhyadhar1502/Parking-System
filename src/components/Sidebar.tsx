import React from 'react';
import { 
  LayoutDashboard, 
  MapPin, 
  Grid3X3, 
  Users, 
  BarChart3, 
  QrCode,
  ShieldCheck,
  UserCheck
} from 'lucide-react';
import { Role } from '../types';

export type NavTab = 
  | 'control_center' 
  | 'locations' 
  | 'slots' 
  | 'gate' 
  | 'users' 
  | 'analytics';

interface SidebarProps {
  currentTab: NavTab;
  onTabChange: (tab: NavTab) => void;
  role: Role;
  onRoleToggle: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentTab,
  onTabChange,
  role,
  onRoleToggle,
}) => {
  const navItems = [
    {
      id: 'control_center' as NavTab,
      label: 'Control Center',
      icon: LayoutDashboard,
    },
    {
      id: 'locations' as NavTab,
      label: 'Parking Locations',
      icon: MapPin,
    },
    {
      id: 'slots' as NavTab,
      label: 'Slot Management',
      icon: Grid3X3,
    },
    {
      id: 'gate' as NavTab,
      label: 'Gate Terminal & QR',
      icon: QrCode,
    },
    {
      id: 'users' as NavTab,
      label: 'User Database',
      icon: Users,
    },
    {
      id: 'analytics' as NavTab,
      label: 'Revenue Analytics',
      icon: BarChart3,
    },
  ];

  return (
    <aside id="app-sidebar" className="w-[240px] bg-[#0F172A] text-white flex flex-col border-r border-[#334155] shrink-0 select-none">
      {/* Brand Header */}
      <div className="p-6 border-b border-[#1E293B]">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-[#38BDF8] rounded-md flex items-center justify-center font-black text-[#0F172A] text-lg shadow-sm">
            P
          </div>
          <h1 className="text-lg font-bold tracking-tight">
            ParkSmart <span className="text-[#38BDF8]">Pro</span>
          </h1>
        </div>
        <div className="flex items-center justify-between mt-2">
          <p className="text-[10px] text-[#94A3B8] uppercase tracking-widest font-semibold">
            {role === 'ADMIN' ? 'College Admin Portal' : 'Student Driver Portal'}
          </p>
          <button
            id="role-toggle-btn"
            onClick={onRoleToggle}
            className="text-[9px] px-2 py-0.5 rounded bg-[#1E293B] text-[#38BDF8] hover:bg-[#334155] border border-[#334155] transition-colors flex items-center gap-1 font-medium"
            title="Switch between Administrator and Customer view"
          >
            {role === 'ADMIN' ? <ShieldCheck className="w-3 h-3" /> : <UserCheck className="w-3 h-3" />}
            <span>{role === 'ADMIN' ? 'Admin' : 'Driver'}</span>
          </button>
        </div>
      </div>

      {/* Navigation Menu */}
      <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
        {navItems.map((item) => {
          const isActive = currentTab === item.id;
          const Icon = item.icon;

          return (
            <button
              key={item.id}
              id={`nav-${item.id}`}
              onClick={() => onTabChange(item.id)}
              className={`w-full text-left p-3 rounded-lg flex items-center gap-3 transition-all cursor-pointer ${
                isActive
                  ? 'bg-[#1E293B] text-white shadow-sm font-medium'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E293B]/50'
              }`}
            >
              {isActive ? (
                <div className="w-1 h-4 bg-[#38BDF8] rounded-full shrink-0"></div>
              ) : (
                <div className="w-1 h-4 bg-transparent shrink-0"></div>
              )}
              <Icon className={`w-4 h-4 shrink-0 ${isActive ? 'text-[#38BDF8]' : 'text-[#64748B]'}`} />
              <span className="text-sm truncate">{item.label}</span>
            </button>
          );
        })}
      </nav>

      {/* System Status Block (from theme) */}
      <div className="p-5 bg-[#1E293B] m-4 rounded-xl border border-[#334155] text-left">
        <p className="text-[10px] text-[#94A3B8] uppercase font-bold tracking-wider mb-2">
          System Status
        </p>
        <div className="flex items-center gap-2">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
          <span className="text-xs font-mono text-slate-300">DB: Connected (MySQL)</span>
        </div>
        <div className="flex items-center gap-2 mt-1.5">
          <span className="relative flex h-2 w-2">
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
          <span className="text-xs font-mono text-slate-300">G-Maps: Active</span>
        </div>
        <div className="flex items-center gap-2 mt-1.5">
          <span className="relative flex h-2 w-2">
            <span className="relative inline-flex rounded-full h-2 w-2 bg-[#38BDF8]"></span>
          </span>
          <span className="text-xs font-mono text-slate-300">IoT Gateway: Ready</span>
        </div>
      </div>
    </aside>
  );
};
