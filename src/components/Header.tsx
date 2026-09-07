import React from 'react';
import { Role } from '../types';
import { Sparkles } from 'lucide-react';

interface HeaderProps {
  title: string;
  subtitle: string;
  role: Role;
  onQuickBookClick: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  title,
  subtitle,
  role,
  onQuickBookClick,
}) => {
  const userName = role === 'ADMIN' ? 'Dr. Sarah Jenkins' : 'John Doe';
  const userTitle = role === 'ADMIN' ? 'Senior Administrator' : 'Computer Science • Class 2026';
  const initials = role === 'ADMIN' ? 'SJ' : 'JD';

  return (
    <header id="app-header" className="h-16 bg-white border-b border-[#E2E8F0] px-8 flex items-center justify-between shrink-0">
      <div className="flex flex-col">
        <h2 className="text-xl font-bold text-[#0F172A] tracking-tight">{title}</h2>
        <p className="text-xs text-[#64748B]">{subtitle}</p>
      </div>

      <div className="flex items-center gap-5">
        <button
          id="header-quick-book-btn"
          onClick={onQuickBookClick}
          className="px-3.5 py-1.5 bg-[#0F172A] text-white text-xs font-semibold rounded-lg hover:bg-[#1E293B] transition-colors shadow-sm flex items-center gap-1.5"
        >
          <Sparkles className="w-3.5 h-3.5 text-[#38BDF8]" />
          <span>New Reservation</span>
        </button>

        <div className="h-8 w-px bg-[#E2E8F0]" />

        <div className="flex items-center gap-3">
          <div className="text-right">
            <p className="text-xs font-bold text-[#0F172A]">{userName}</p>
            <p className="text-[10px] text-[#64748B] uppercase font-medium tracking-wide">{userTitle}</p>
          </div>
          <div className="w-10 h-10 bg-[#E2E8F0] rounded-full border-2 border-[#38BDF8] flex items-center justify-center font-bold text-xs text-[#0F172A] shadow-sm">
            {initials}
          </div>
        </div>
      </div>
    </header>
  );
};
