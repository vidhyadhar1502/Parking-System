import React, { useState } from 'react';
import { SystemUser, Reservation } from '../types';
import { Search, UserCheck, Shield, QrCode, Phone, Mail, Car, Calendar } from 'lucide-react';

interface UserDatabaseViewProps {
  users: SystemUser[];
  reservations: Reservation[];
  onOpenQRBadge: (res: Reservation) => void;
}

export const UserDatabaseView: React.FC<UserDatabaseViewProps> = ({
  users,
  reservations,
  onOpenQRBadge,
}) => {
  const [search, setSearch] = useState('');
  const [selectedUser, setSelectedUser] = useState<SystemUser>(users[0]);

  const filteredUsers = users.filter(
    (u) =>
      u.name.toLowerCase().includes(search.toLowerCase()) ||
      u.email.toLowerCase().includes(search.toLowerCase()) ||
      u.vehicleNumber.toLowerCase().includes(search.toLowerCase())
  );

  const userReservations = reservations.filter((r) => r.userId === selectedUser.id);

  return (
    <div id="user-database-view" className="flex-1 flex flex-col p-8 overflow-y-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-[#0F172A] tracking-tight">
            User Directory & Reservations
          </h2>
          <p className="text-xs text-[#64748B]">
            Registered student, faculty, and administrator accounts connected to MySQL
          </p>
        </div>

        <div className="relative">
          <Search className="w-4 h-4 text-[#64748B] absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Search by name, email, vehicle..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9 pr-4 py-2 bg-white border border-[#E2E8F0] rounded-xl text-xs text-[#0F172A] focus:outline-none focus:border-[#38BDF8] w-64 shadow-xs"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 flex-1 min-h-[500px]">
        {/* User Table (7 cols) */}
        <div className="lg:col-span-7 bg-white rounded-3xl border border-[#E2E8F0] shadow-sm overflow-hidden flex flex-col">
          <div className="p-5 border-b border-[#F1F5F9] flex justify-between items-center">
            <h3 className="font-bold text-sm uppercase tracking-widest text-[#0F172A]">
              Registered System Accounts
            </h3>
            <span className="text-xs font-mono text-[#64748B] bg-[#F1F5F9] px-2.5 py-1 rounded-md">
              {users.length} Total Users
            </span>
          </div>

          <div className="overflow-x-auto flex-1">
            <table className="w-full text-left text-xs">
              <thead className="bg-[#F8FAFC] text-[#64748B] font-bold uppercase tracking-wider border-b border-[#E2E8F0]">
                <tr>
                  <th className="p-4">User</th>
                  <th className="p-4">Role</th>
                  <th className="p-4">Vehicle No.</th>
                  <th className="p-4">Bookings</th>
                  <th className="p-4">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#F1F5F9]">
                {filteredUsers.map((u) => {
                  const isSelected = u.id === selectedUser.id;
                  return (
                    <tr
                      key={u.id}
                      className={`hover:bg-[#F8FAFC] cursor-pointer transition-colors ${
                        isSelected ? 'bg-sky-50/50' : ''
                      }`}
                      onClick={() => setSelectedUser(u)}
                    >
                      <td className="p-4">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 rounded-lg bg-[#0F172A] text-white flex items-center justify-center font-bold text-xs">
                            {u.name.split(' ').map((n) => n[0]).join('')}
                          </div>
                          <div>
                            <p className="font-bold text-[#0F172A]">{u.name}</p>
                            <p className="text-[11px] text-[#64748B]">{u.email}</p>
                          </div>
                        </div>
                      </td>
                      <td className="p-4">
                        <span
                          className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                            u.role === 'ADMIN'
                              ? 'bg-purple-100 text-purple-700'
                              : 'bg-blue-100 text-blue-700'
                          }`}
                        >
                          {u.role}
                        </span>
                      </td>
                      <td className="p-4 font-mono font-bold text-[#0F172A]">
                        {u.vehicleNumber}
                      </td>
                      <td className="p-4 font-semibold text-[#0F172A]">
                        {u.totalBookings}
                      </td>
                      <td className="p-4">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedUser(u);
                          }}
                          className="text-[11px] text-[#38BDF8] font-bold hover:underline"
                        >
                          Inspect
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        {/* Selected User Details & Reservations (5 cols) */}
        <div className="lg:col-span-5 bg-white rounded-3xl border border-[#E2E8F0] shadow-sm p-6 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-4 border-b border-[#F1F5F9]">
              <h3 className="font-bold text-sm uppercase tracking-widest text-[#0F172A]">
                Account Profile & Passes
              </h3>
              <span className="text-[10px] font-mono text-[#64748B]">UID: #{selectedUser.id}</span>
            </div>

            {/* Profile Summary Card */}
            <div className="p-4 bg-[#F8FAFC] rounded-2xl border border-[#E2E8F0] mt-4 space-y-2 text-xs">
              <div className="flex items-center gap-2 text-[#0F172A] font-bold">
                <Mail className="w-3.5 h-3.5 text-[#64748B]" />
                <span>{selectedUser.email}</span>
              </div>
              <div className="flex items-center gap-2 text-[#0F172A] font-bold">
                <Phone className="w-3.5 h-3.5 text-[#64748B]" />
                <span>{selectedUser.phone}</span>
              </div>
              <div className="flex items-center gap-2 text-[#0F172A] font-bold">
                <Car className="w-3.5 h-3.5 text-[#38BDF8]" />
                <span className="font-mono">Primary Plate: {selectedUser.vehicleNumber}</span>
              </div>
            </div>

            {/* User Reservations */}
            <div className="mt-5">
              <span className="text-xs font-bold text-[#0F172A] uppercase tracking-wider block mb-3">
                Associated Reservations ({userReservations.length})
              </span>

              <div className="space-y-3 max-h-[260px] overflow-y-auto pr-1">
                {userReservations.length === 0 ? (
                  <p className="text-xs text-[#64748B] italic">No active reservation records found.</p>
                ) : (
                  userReservations.map((res) => (
                    <div
                      key={res.id}
                      className="p-3 bg-white border border-[#E2E8F0] rounded-xl hover:border-[#38BDF8] transition-colors flex items-center justify-between"
                    >
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-mono font-bold text-xs text-[#0F172A]">
                            {res.token}
                          </span>
                          <span
                            className={`text-[9px] px-2 py-0.5 rounded-full font-bold uppercase ${
                              res.status === 'CONFIRMED'
                                ? 'bg-amber-100 text-amber-800'
                                : res.status === 'CHECKED_IN'
                                ? 'bg-emerald-100 text-emerald-800'
                                : 'bg-slate-100 text-slate-800'
                            }`}
                          >
                            {res.status}
                          </span>
                        </div>
                        <p className="text-[11px] text-[#64748B] mt-0.5">
                          {res.locationName} • Bay {res.slotId}
                        </p>
                      </div>

                      <button
                        onClick={() => onOpenQRBadge(res)}
                        className="px-2.5 py-1.5 bg-[#0F172A] text-white rounded-lg text-[10px] font-bold hover:bg-[#1E293B] flex items-center gap-1 cursor-pointer"
                        title="Display QR code verification pass"
                      >
                        <QrCode className="w-3 h-3 text-[#38BDF8]" />
                        <span>View QR</span>
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>

          <div className="p-3 bg-[#F1F5F9] rounded-xl border border-[#E2E8F0] text-[10px] text-[#64748B] mt-4">
            Security audit: All sessions encrypted via BCrypt hashed storage.
          </div>
        </div>
      </div>
    </div>
  );
};
