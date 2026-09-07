export type Role = 'ADMIN' | 'CUSTOMER';

export type SlotType = 'STANDARD' | 'COMPACT' | 'EV_CHARGING' | 'HANDICAPPED';

export type SlotStatus = 'AVAILABLE' | 'RESERVED' | 'OCCUPIED' | 'MAINTENANCE';

export type ReservationStatus = 'CONFIRMED' | 'CHECKED_IN' | 'COMPLETED' | 'CANCELLED';

export interface ParkingLocation {
  id: number;
  name: string;
  code: string;
  address: string;
  latitude: number;
  longitude: number;
  totalCapacity: number;
  hourlyRate: number;
  floors: number;
  isActive: boolean;
}

export interface ParkingSlot {
  id: string; // e.g. "A01"
  locationId: number;
  floor: number;
  slotNumber: string;
  type: SlotType;
  status: SlotStatus;
  occupiedBy?: string; // Vehicle number or user name
  reservedUntil?: string;
}

export interface Reservation {
  id: string; // UUID
  token: string;
  userId: number;
  userName: string;
  userEmail: string;
  vehicleNumber: string;
  locationId: number;
  locationName: string;
  slotId: string;
  floor: number;
  reservationTime: string;
  checkInTime?: string;
  checkOutTime?: string;
  status: ReservationStatus;
  durationHours: number;
  totalAmount: number;
  paid: boolean;
}

export interface ActivityLog {
  id: string;
  userName: string;
  initials: string;
  action: string;
  detail: string;
  time: string;
  type: 'checkin' | 'checkout' | 'reserve' | 'qr_verify' | 'system';
}

export interface SystemUser {
  id: number;
  name: string;
  email: string;
  role: Role;
  phone: string;
  vehicleNumber: string;
  totalBookings: number;
}
