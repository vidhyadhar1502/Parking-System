/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { Sidebar, NavTab } from './components/Sidebar';
import { Header } from './components/Header';
import { ControlCenter } from './components/ControlCenter';
import { ParkingLocationsView } from './components/ParkingLocationsView';
import { SlotManagementView } from './components/SlotManagementView';
import { GateSimulatorView } from './components/GateSimulatorView';
import { UserDatabaseView } from './components/UserDatabaseView';
import { RevenueAnalyticsView } from './components/RevenueAnalyticsView';
import { ReservationModal } from './components/ReservationModal';
import { QRCodeModal } from './components/QRCodeModal';
import { SlotDetailModal } from './components/SlotDetailModal';
import { 
  INITIAL_LOCATIONS, 
  generateInitialSlots, 
  INITIAL_USERS, 
  INITIAL_RESERVATIONS, 
  INITIAL_ACTIVITY 
} from './mockData';
import { Role, ParkingSlot, Reservation, SlotStatus, ActivityLog } from './types';

export default function App() {
  const [currentTab, setCurrentTab] = useState<NavTab>('control_center');
  const [role, setRole] = useState<Role>('ADMIN');

  // Core system data states (simulating MySQL database tables)
  const [locations, setLocations] = useState(INITIAL_LOCATIONS);
  const [slots, setSlots] = useState<ParkingSlot[]>(generateInitialSlots);
  const [users, setUsers] = useState(INITIAL_USERS);
  const [reservations, setReservations] = useState<Reservation[]>(INITIAL_RESERVATIONS);
  const [activityLogs, setActivityLogs] = useState<ActivityLog[]>(INITIAL_ACTIVITY);

  // Active location selection
  const [selectedLocationId, setSelectedLocationId] = useState<number>(1);

  // Modals state
  const [isReservationModalOpen, setIsReservationModalOpen] = useState(false);
  const [preselectedSlot, setPreselectedSlot] = useState<ParkingSlot | null>(null);
  const [activeQRReservation, setActiveQRReservation] = useState<Reservation | null>(null);
  const [inspectedSlot, setInspectedSlot] = useState<ParkingSlot | null>(null);

  // Update Slot Status (Available, Occupied, Reserved, Maintenance)
  const handleUpdateSlotStatus = (slotId: string, newStatus: SlotStatus) => {
    setSlots((prev) =>
      prev.map((s) => {
        if (s.id === slotId) {
          return {
            ...s,
            status: newStatus,
            occupiedBy: newStatus === 'OCCUPIED' ? 'SIM-VEH' : newStatus === 'RESERVED' ? 'Reserved' : undefined,
          };
        }
        return s;
      })
    );

    // Append activity log
    const targetSlot = slots.find((s) => s.id === slotId);
    if (targetSlot) {
      const newLog: ActivityLog = {
        id: `act-${Date.now()}`,
        userName: role === 'ADMIN' ? 'Dr. Sarah Jenkins' : 'Driver',
        initials: role === 'ADMIN' ? 'SJ' : 'DR',
        action: newStatus === 'AVAILABLE' ? 'Slot Released' : `Status -> ${newStatus}`,
        detail: `Bay ${targetSlot.slotNumber} • Deck ${targetSlot.floor}`,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        type: 'system',
      };
      setActivityLogs((prev) => [newLog, ...prev.slice(0, 9)]);
    }
  };

  // Add new Slot
  const handleAddSlot = (newSlotData: Partial<ParkingSlot>) => {
    const newId = `L${newSlotData.floor}-${newSlotData.slotNumber}`;
    const newSlot: ParkingSlot = {
      id: newId,
      locationId: newSlotData.locationId || 1,
      floor: newSlotData.floor || 1,
      slotNumber: newSlotData.slotNumber || 'NEW',
      type: newSlotData.type || 'STANDARD',
      status: 'AVAILABLE',
    };
    setSlots((prev) => [...prev, newSlot]);
  };

  // Complete a new reservation
  const handleConfirmReservation = (data: {
    locationId: number;
    floor: number;
    slotId: string;
    vehicleNumber: string;
    userName: string;
    userEmail: string;
    durationHours: number;
  }) => {
    const location = locations.find((l) => l.id === data.locationId) || locations[0];
    const generatedToken = `PK-${Math.floor(10000 + Math.random() * 90000)}`;
    const totalAmount = data.durationHours * location.hourlyRate;

    const newRes: Reservation = {
      id: `res-${Date.now()}`,
      token: generatedToken,
      userId: 2, // John Doe / default
      userName: data.userName,
      userEmail: data.userEmail,
      vehicleNumber: data.vehicleNumber,
      locationId: data.locationId,
      locationName: location.name,
      slotId: data.slotId,
      floor: data.floor,
      reservationTime: new Date().toISOString().replace('T', ' ').slice(0, 16),
      status: 'CONFIRMED',
      durationHours: data.durationHours,
      totalAmount,
      paid: true,
    };

    // Update reservation state
    setReservations((prev) => [newRes, ...prev]);

    // Mark slot as RESERVED
    setSlots((prev) =>
      prev.map((s) => (s.id === data.slotId ? { ...s, status: 'RESERVED', occupiedBy: data.vehicleNumber } : s))
    );

    // Append log
    const targetSlot = slots.find((s) => s.id === data.slotId);
    const newLog: ActivityLog = {
      id: `act-${Date.now()}`,
      userName: data.userName,
      initials: data.userName.split(' ').map((n) => n[0]).join(''),
      action: 'Reservation Made',
      detail: `Bay ${targetSlot?.slotNumber || data.slotId} • ${generatedToken}`,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      type: 'reserve',
    };
    setActivityLogs((prev) => [newLog, ...prev.slice(0, 9)]);

    setIsReservationModalOpen(false);
    setPreselectedSlot(null);
    setActiveQRReservation(newRes);
  };

  // Gate check-in
  const handleCheckIn = (token: string) => {
    const res = reservations.find(
      (r) => r.token.toUpperCase() === token.toUpperCase()
    );

    if (!res) {
      return { success: false, message: `Reservation token ${token} not found in database.` };
    }

    if (res.status === 'CHECKED_IN') {
      return { success: false, message: `Vehicle is already checked into slot ${res.slotId}.` };
    }

    if (res.status === 'COMPLETED') {
      return { success: false, message: `Token ${token} has already been checked out and completed.` };
    }

    // Update reservation
    setReservations((prev) =>
      prev.map((r) =>
        r.id === res.id
          ? {
              ...r,
              status: 'CHECKED_IN',
              checkInTime: new Date().toISOString().replace('T', ' ').slice(0, 16),
            }
          : r
      )
    );

    // Update slot to OCCUPIED
    setSlots((prev) =>
      prev.map((s) =>
        s.id === res.slotId
          ? { ...s, status: 'OCCUPIED', occupiedBy: res.vehicleNumber }
          : s
      )
    );

    // Log activity
    const newLog: ActivityLog = {
      id: `act-${Date.now()}`,
      userName: res.userName,
      initials: res.userName.split(' ').map((n) => n[0]).join(''),
      action: 'Checked-in',
      detail: `Slot ${res.slotId} • Barrier Open`,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      type: 'checkin',
    };
    setActivityLogs((prev) => [newLog, ...prev.slice(0, 9)]);

    return {
      success: true,
      message: `Verified! Welcome ${res.userName}. Barrier opened for Bay ${res.slotId}.`,
      reservation: res,
    };
  };

  // Gate check-out
  const handleCheckOut = (token: string) => {
    const res = reservations.find(
      (r) => r.token.toUpperCase() === token.toUpperCase()
    );

    if (!res) {
      return { success: false, message: `Reservation token ${token} not found.` };
    }

    const parkedHours = 2.5;
    const location = locations.find((l) => l.id === res.locationId) || locations[0];
    const computedFee = Math.ceil(parkedHours) * location.hourlyRate;

    // Update reservation
    setReservations((prev) =>
      prev.map((r) =>
        r.id === res.id
          ? {
              ...r,
              status: 'COMPLETED',
              checkOutTime: new Date().toISOString().replace('T', ' ').slice(0, 16),
              totalAmount: computedFee,
              paid: true,
            }
          : r
      )
    );

    // Free the slot
    setSlots((prev) =>
      prev.map((s) =>
        s.id === res.slotId
          ? { ...s, status: 'AVAILABLE', occupiedBy: undefined }
          : s
      )
    );

    // Log activity
    const newLog: ActivityLog = {
      id: `act-${Date.now()}`,
      userName: res.userName,
      initials: res.userName.split(' ').map((n) => n[0]).join(''),
      action: 'Checked-out',
      detail: `$${computedFee.toFixed(2)} Paid • Barrier Cleared`,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      type: 'checkout',
    };
    setActivityLogs((prev) => [newLog, ...prev.slice(0, 9)]);

    return {
      success: true,
      message: `Payment of $${computedFee.toFixed(2)} confirmed. Exit barrier opened. Safe travels!`,
      fee: computedFee,
      hours: parkedHours,
    };
  };

  const getHeaderMeta = () => {
    switch (currentTab) {
      case 'control_center':
        return {
          title: 'Dashboard Overview',
          subtitle: 'Real-time parking synchronization: Campus Block A',
        };
      case 'locations':
        return {
          title: 'Google Maps Parking Discovery',
          subtitle: 'Real-time spatial facility locator & coordinate map',
        };
      case 'slots':
        return {
          title: 'Slot Inventory & Management',
          subtitle: 'Configure multi-level bays, EV charging stations, and maintenance locks',
        };
      case 'gate':
        return {
          title: 'Gate Barrier Terminal Simulator',
          subtitle: 'Digital QR validation, automated duration timestamping, and billing',
        };
      case 'users':
        return {
          title: 'Campus User Directory',
          subtitle: 'Registered driver records, vehicle plates, and active booking badges',
        };
      case 'analytics':
        return {
          title: 'Revenue & Occupancy Analytics',
          subtitle: 'Throughput velocity, facility performance, and CSV financial audits',
        };
    }
  };

  const headerMeta = getHeaderMeta();

  return (
    <div className="flex h-screen w-screen bg-[#F1F5F9] text-[#1E293B] font-sans overflow-hidden select-none">
      {/* Sidebar with Geometric Balance styling */}
      <Sidebar
        currentTab={currentTab}
        onTabChange={(tab) => setCurrentTab(tab)}
        role={role}
        onRoleToggle={() => setRole(role === 'ADMIN' ? 'CUSTOMER' : 'ADMIN')}
      />

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Prototype Isolation Notice Banner */}
        <div className="bg-[#0F172A] border-b border-[#334155] px-4 py-1.5 flex items-center justify-between text-[11px] text-[#94A3B8] select-none">
          <div className="flex items-center gap-2">
            <span className="px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 font-mono font-bold text-[10px]">
              PROTOTYPE NOTICE
            </span>
            <span>
              This React view serves strictly as a <strong>UI/functional prototype</strong>. Production application runs on <strong>Java 17/21 • JavaFX 21 • MySQL 8.x • JDBC • HikariCP</strong> in <code className="text-[#38BDF8]">src/main/java</code> and <code className="text-[#38BDF8]">database/</code>.
            </span>
          </div>
          <span className="hidden md:inline text-[10px] text-[#64748B]">Phase 1: Setup &amp; DB Foundations Active</span>
        </div>

        {/* Header */}
        <Header
          title={headerMeta.title}
          subtitle={headerMeta.subtitle}
          role={role}
          onQuickBookClick={() => {
            setPreselectedSlot(null);
            setIsReservationModalOpen(true);
          }}
        />

        {/* Dynamic Views */}
        {currentTab === 'control_center' && (
          <ControlCenter
            slots={slots}
            locations={locations}
            activityLogs={activityLogs}
            onSlotClick={(slot) => setInspectedSlot(slot)}
            onViewAllLogs={() => setCurrentTab('analytics')}
          />
        )}

        {currentTab === 'locations' && (
          <ParkingLocationsView
            locations={locations}
            selectedLocationId={selectedLocationId}
            onSelectLocation={(id) => setSelectedLocationId(id)}
            onNavigateToSlots={(id) => {
              setSelectedLocationId(id);
              setCurrentTab('slots');
            }}
          />
        )}

        {currentTab === 'slots' && (
          <SlotManagementView
            slots={slots}
            locations={locations}
            onUpdateSlotStatus={handleUpdateSlotStatus}
            onAddSlot={handleAddSlot}
          />
        )}

        {currentTab === 'gate' && (
          <GateSimulatorView
            reservations={reservations}
            slots={slots}
            onCheckIn={handleCheckIn}
            onCheckOut={handleCheckOut}
          />
        )}

        {currentTab === 'users' && (
          <UserDatabaseView
            users={users}
            reservations={reservations}
            onOpenQRBadge={(res) => setActiveQRReservation(res)}
          />
        )}

        {currentTab === 'analytics' && (
          <RevenueAnalyticsView locations={locations} />
        )}
      </main>

      {/* Modals */}
      {isReservationModalOpen && (
        <ReservationModal
          locations={locations}
          slots={slots}
          preselectedSlot={preselectedSlot}
          onClose={() => {
            setIsReservationModalOpen(false);
            setPreselectedSlot(null);
          }}
          onConfirmReservation={handleConfirmReservation}
        />
      )}

      {activeQRReservation && (
        <QRCodeModal
          reservation={activeQRReservation}
          onClose={() => setActiveQRReservation(null)}
          onGoToGate={(token) => {
            setActiveQRReservation(null);
            setCurrentTab('gate');
          }}
        />
      )}

      {inspectedSlot && (
        <SlotDetailModal
          slot={inspectedSlot}
          location={locations.find((l) => l.id === inspectedSlot.locationId) || locations[0]}
          onClose={() => setInspectedSlot(null)}
          onUpdateStatus={handleUpdateSlotStatus}
          onStartReservation={(slot) => {
            setPreselectedSlot(slot);
            setIsReservationModalOpen(true);
          }}
        />
      )}
    </div>
  );
}
