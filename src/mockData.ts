import { ParkingLocation, ParkingSlot, Reservation, ActivityLog, SystemUser } from './types';

export const INITIAL_LOCATIONS: ParkingLocation[] = [
  {
    id: 1,
    name: 'Campus Block A - Central Deck',
    code: 'ZONE-A',
    address: '400 Innovation Way, North Campus, Building A',
    latitude: 37.4221,
    longitude: -122.0841,
    totalCapacity: 48,
    hourlyRate: 3.5,
    floors: 2,
    isActive: true,
  },
  {
    id: 2,
    name: 'Engineering & Tech Hub Plaza',
    code: 'ZONE-B',
    address: '120 Science Drive, West Campus',
    latitude: 37.4245,
    longitude: -122.0862,
    totalCapacity: 64,
    hourlyRate: 4.0,
    floors: 3,
    isActive: true,
  },
  {
    id: 3,
    name: 'South Sports Arena Lot',
    code: 'ZONE-C',
    address: '850 Varsity Blvd, South Campus',
    latitude: 37.4198,
    longitude: -122.0815,
    totalCapacity: 120,
    hourlyRate: 2.0,
    floors: 1,
    isActive: true,
  },
  {
    id: 4,
    name: 'Medical Sciences Center',
    code: 'ZONE-D',
    address: '210 Health Ave, East Campus',
    latitude: 37.4268,
    longitude: -122.0802,
    totalCapacity: 80,
    hourlyRate: 4.5,
    floors: 2,
    isActive: true,
  },
];

// Helper to generate slots for Zone A matching the design:
// Row A: A01 to A08
// Row B: B01 to B08
// Driveway exit
// Row C: C01 to C08
export const generateInitialSlots = (): ParkingSlot[] => {
  const slots: ParkingSlot[] = [];

  // Level 1 of Location 1
  const level1Config: Record<string, { status: 'AVAILABLE' | 'OCCUPIED' | 'RESERVED'; type: any }> = {
    A01: { status: 'AVAILABLE', type: 'STANDARD' },
    A02: { status: 'OCCUPIED', type: 'STANDARD' },
    A03: { status: 'OCCUPIED', type: 'STANDARD' },
    A04: { status: 'AVAILABLE', type: 'COMPACT' },
    A05: { status: 'RESERVED', type: 'EV_CHARGING' },
    A06: { status: 'AVAILABLE', type: 'STANDARD' },
    A07: { status: 'OCCUPIED', type: 'STANDARD' },
    A08: { status: 'AVAILABLE', type: 'HANDICAPPED' },

    B01: { status: 'AVAILABLE', type: 'STANDARD' },
    B02: { status: 'OCCUPIED', type: 'STANDARD' },
    B03: { status: 'AVAILABLE', type: 'STANDARD' },
    B04: { status: 'AVAILABLE', type: 'COMPACT' },
    B05: { status: 'OCCUPIED', type: 'STANDARD' },
    B06: { status: 'AVAILABLE', type: 'STANDARD' },
    B07: { status: 'AVAILABLE', type: 'EV_CHARGING' },
    B08: { status: 'AVAILABLE', type: 'HANDICAPPED' },

    C01: { status: 'AVAILABLE', type: 'STANDARD' },
    C02: { status: 'AVAILABLE', type: 'STANDARD' },
    C03: { status: 'OCCUPIED', type: 'STANDARD' },
    C04: { status: 'OCCUPIED', type: 'STANDARD' },
    C05: { status: 'AVAILABLE', type: 'COMPACT' },
    C06: { status: 'AVAILABLE', type: 'STANDARD' },
    C07: { status: 'OCCUPIED', type: 'EV_CHARGING' },
    C08: { status: 'AVAILABLE', type: 'STANDARD' },
  };

  Object.entries(level1Config).forEach(([code, cfg]) => {
    slots.push({
      id: `L1-${code}`,
      locationId: 1,
      floor: 1,
      slotNumber: code,
      type: cfg.type,
      status: cfg.status,
      occupiedBy: cfg.status === 'OCCUPIED' ? 'CAL-8924' : cfg.status === 'RESERVED' ? 'Reserved' : undefined,
    });
  });

  // Level 2 of Location 1
  const level2Codes = ['A01', 'A02', 'A03', 'A04', 'A05', 'A06', 'A07', 'A08', 'B01', 'B02', 'B03', 'B04', 'B05', 'B06', 'B07', 'B08', 'C01', 'C02', 'C03', 'C04', 'C05', 'C06', 'C07', 'C08'];
  level2Codes.forEach((code, i) => {
    const isOccupied = i % 3 === 0;
    const isReserved = i === 5 || i === 11;
    slots.push({
      id: `L2-${code}`,
      locationId: 1,
      floor: 2,
      slotNumber: code,
      type: i % 4 === 0 ? 'EV_CHARGING' : i === 7 ? 'HANDICAPPED' : 'STANDARD',
      status: isOccupied ? 'OCCUPIED' : isReserved ? 'RESERVED' : 'AVAILABLE',
      occupiedBy: isOccupied ? `VEH-7${i}9` : undefined,
    });
  });

  return slots;
};

export const INITIAL_USERS: SystemUser[] = [
  {
    id: 1,
    name: 'Dr. Sarah Jenkins',
    email: 's.jenkins@campus.edu',
    role: 'ADMIN',
    phone: '+1 (555) 234-5678',
    vehicleNumber: 'ADM-1001',
    totalBookings: 14,
  },
  {
    id: 2,
    name: 'John Doe',
    email: 'john.doe@student.edu',
    role: 'CUSTOMER',
    phone: '+1 (555) 987-6543',
    vehicleNumber: 'CAL-8924',
    totalBookings: 8,
  },
  {
    id: 3,
    name: 'Maria Smith',
    email: 'maria.s@student.edu',
    role: 'CUSTOMER',
    phone: '+1 (555) 456-7890',
    vehicleNumber: 'TX-4321',
    totalBookings: 12,
  },
  {
    id: 4,
    name: 'Ryan King',
    email: 'r.king@faculty.edu',
    role: 'CUSTOMER',
    phone: '+1 (555) 321-7654',
    vehicleNumber: 'NY-8832',
    totalBookings: 25,
  },
  {
    id: 5,
    name: 'Anna Lee',
    email: 'anna.lee@student.edu',
    role: 'CUSTOMER',
    phone: '+1 (555) 654-0987',
    vehicleNumber: 'WA-5541',
    totalBookings: 5,
  },
];

export const INITIAL_RESERVATIONS: Reservation[] = [
  {
    id: 'res-101',
    token: 'PK-98214',
    userId: 2,
    userName: 'John Doe',
    userEmail: 'john.doe@student.edu',
    vehicleNumber: 'CAL-8924',
    locationId: 1,
    locationName: 'Campus Block A - Central Deck',
    slotId: 'L1-A03',
    floor: 1,
    reservationTime: '2026-09-06 13:00',
    checkInTime: '2026-09-06 13:15',
    status: 'CHECKED_IN',
    durationHours: 3,
    totalAmount: 10.5,
    paid: false,
  },
  {
    id: 'res-102',
    token: 'PK-44129',
    userId: 3,
    userName: 'Maria Smith',
    userEmail: 'maria.s@student.edu',
    vehicleNumber: 'TX-4321',
    locationId: 1,
    locationName: 'Campus Block A - Central Deck',
    slotId: 'L1-A05',
    floor: 1,
    reservationTime: '2026-09-06 14:38',
    status: 'CONFIRMED',
    durationHours: 2,
    totalAmount: 7.0,
    paid: true,
  },
  {
    id: 'res-103',
    token: 'PK-11048',
    userId: 4,
    userName: 'Ryan King',
    userEmail: 'r.king@faculty.edu',
    vehicleNumber: 'NY-8832',
    locationId: 1,
    locationName: 'Campus Block A - Central Deck',
    slotId: 'L1-B05',
    floor: 1,
    reservationTime: '2026-09-06 11:30',
    checkInTime: '2026-09-06 11:45',
    checkOutTime: '2026-09-06 14:30',
    status: 'COMPLETED',
    durationHours: 2.75,
    totalAmount: 12.5,
    paid: true,
  },
];

export const INITIAL_ACTIVITY: ActivityLog[] = [
  {
    id: 'act-1',
    userName: 'John Doe',
    initials: 'JD',
    action: 'Checked-in',
    detail: 'Slot A-03 • CAL-8924',
    time: '14:42',
    type: 'checkin',
  },
  {
    id: 'act-2',
    userName: 'Maria Smith',
    initials: 'MS',
    action: 'Reservation Made',
    detail: 'Slot A-05 • Level 01',
    time: '14:38',
    type: 'reserve',
  },
  {
    id: 'act-3',
    userName: 'Ryan King',
    initials: 'RK',
    action: 'Checked-out',
    detail: '$12.50 Paid • Gate B Barrier Cleared',
    time: '14:30',
    type: 'checkout',
  },
  {
    id: 'act-4',
    userName: 'Anna Lee',
    initials: 'AL',
    action: 'QR Code Verified',
    detail: 'Token PK-77291 Validated at Entry',
    time: '14:22',
    type: 'qr_verify',
  },
  {
    id: 'act-5',
    userName: 'System Monitor',
    initials: 'SM',
    action: 'Simulated Slot Sync',
    detail: 'HikariCP Pool: 10/10 active connections',
    time: '14:15',
    type: 'system',
  },
];
