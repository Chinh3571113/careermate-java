# Calendar UI Gray-Out Implementation Guide

## Overview

This guide shows how to implement a calendar day view where time slots **outside working hours** are grayed out (disabled), while time slots **within working hours** remain active and clickable.

**Example**: If a recruiter works 8 AM - 5 PM, then:
- ✅ **8:00 AM - 5:00 PM**: Normal/active (white background, clickable)
- ⚠️ **12:00 PM - 1:00 PM**: Lunch break (yellow/orange, unavailable)
- ❌ **12:00 AM - 8:00 AM**: Before work hours (gray, disabled)
- ❌ **5:00 PM - 11:59 PM**: After work hours (gray, disabled)

---

## Step 1: Fetch Working Hours Data

### API Endpoint

```
GET /api/v1/interview-calendar/recruiters/working-hours
```

**Authentication**: JWT token with `RECRUITER` role

### Request Example

```typescript
import axios from 'axios';

interface WorkingHoursResponse {
  id: number;
  recruiterId: number;
  dayOfWeek: string;  // "MONDAY", "TUESDAY", etc.
  isWorkingDay: boolean;
  startTime: string;  // "08:00:00"
  endTime: string;    // "17:00:00"
  lunchBreakStart: string;  // "12:00:00"
  lunchBreakEnd: string;    // "13:00:00"
  bufferMinutesBetweenInterviews: number;
  maxInterviewsPerDay: number;
}

async function fetchWorkingHours(): Promise<WorkingHoursResponse[]> {
  const token = localStorage.getItem('jwtToken');
  
  const response = await axios.get(
    '/api/v1/interview-calendar/recruiters/working-hours',
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  return response.data;
}
```

### Example Response

```json
[
  {
    "id": 1,
    "recruiterId": 123,
    "dayOfWeek": "MONDAY",
    "isWorkingDay": true,
    "startTime": "08:00:00",
    "endTime": "17:00:00",
    "lunchBreakStart": "12:00:00",
    "lunchBreakEnd": "13:00:00",
    "bufferMinutesBetweenInterviews": 15,
    "maxInterviewsPerDay": 8
  },
  {
    "id": 2,
    "recruiterId": 123,
    "dayOfWeek": "TUESDAY",
    "isWorkingDay": true,
    "startTime": "08:00:00",
    "endTime": "17:00:00",
    "lunchBreakStart": "12:00:00",
    "lunchBreakEnd": "13:00:00",
    "bufferMinutesBetweenInterviews": 15,
    "maxInterviewsPerDay": 8
  },
  {
    "id": 7,
    "recruiterId": 123,
    "dayOfWeek": "SATURDAY",
    "isWorkingDay": false,
    "startTime": null,
    "endTime": null,
    "lunchBreakStart": null,
    "lunchBreakEnd": null,
    "bufferMinutesBetweenInterviews": 0,
    "maxInterviewsPerDay": 0
  }
]
```

---

## Step 2: Parse Time Strings

### Helper Functions

```typescript
/**
 * Converts "HH:MM:SS" string to minutes since midnight
 * Example: "08:00:00" → 480 (8 * 60)
 *          "17:00:00" → 1020 (17 * 60)
 */
function timeStringToMinutes(timeString: string): number {
  const [hours, minutes] = timeString.split(':').map(Number);
  return hours * 60 + minutes;
}

/**
 * Converts minutes since midnight to "HH:MM" display format
 * Example: 480 → "08:00"
 *          1020 → "17:00"
 */
function minutesToTimeString(minutes: number): string {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
}

/**
 * Creates a Date object for a specific time on a given date
 */
function createTimeOnDate(date: Date, timeString: string): Date {
  const [hours, minutes, seconds] = timeString.split(':').map(Number);
  const result = new Date(date);
  result.setHours(hours, minutes, seconds || 0, 0);
  return result;
}
```

---

## Step 3: Build Time Slot Configuration

### Create Working Hours Map

```typescript
interface TimeSlotConfig {
  dayOfWeek: string;
  isWorkingDay: boolean;
  workStart: number;      // Minutes since midnight
  workEnd: number;        // Minutes since midnight
  lunchStart: number | null;
  lunchEnd: number | null;
}

/**
 * Converts API response to a map for quick lookups
 */
function buildWorkingHoursMap(
  workingHours: WorkingHoursResponse[]
): Map<string, TimeSlotConfig> {
  const map = new Map<string, TimeSlotConfig>();
  
  workingHours.forEach(wh => {
    map.set(wh.dayOfWeek, {
      dayOfWeek: wh.dayOfWeek,
      isWorkingDay: wh.isWorkingDay,
      workStart: wh.startTime ? timeStringToMinutes(wh.startTime) : 0,
      workEnd: wh.endTime ? timeStringToMinutes(wh.endTime) : 1440, // 24:00
      lunchStart: wh.lunchBreakStart ? timeStringToMinutes(wh.lunchBreakStart) : null,
      lunchEnd: wh.lunchBreakEnd ? timeStringToMinutes(wh.lunchBreakEnd) : null,
    });
  });
  
  return map;
}

/**
 * Gets day name from Date object
 */
function getDayOfWeek(date: Date): string {
  const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
  return days[date.getDay()];
}
```

---

## Step 4: Determine Time Slot Status

### Core Logic

```typescript
enum TimeSlotStatus {
  AVAILABLE = 'available',      // Green/white - clickable
  LUNCH_BREAK = 'lunch',        // Yellow/orange - unavailable
  NON_WORKING = 'non-working',  // Gray - disabled
  NON_WORKING_DAY = 'off-day'   // Dark gray - entire day off
}

interface TimeSlotStatusResult {
  status: TimeSlotStatus;
  reason?: string;
  isClickable: boolean;
}

/**
 * Determines the status of a specific time slot
 * 
 * @param date - The date being checked
 * @param timeInMinutes - Time in minutes since midnight (e.g., 480 for 8:00 AM)
 * @param workingHoursMap - Map of working hours by day
 * @returns Status and clickability of the time slot
 */
function getTimeSlotStatus(
  date: Date,
  timeInMinutes: number,
  workingHoursMap: Map<string, TimeSlotConfig>
): TimeSlotStatusResult {
  const dayOfWeek = getDayOfWeek(date);
  const config = workingHoursMap.get(dayOfWeek);
  
  // No configuration found - default to non-working
  if (!config) {
    return {
      status: TimeSlotStatus.NON_WORKING_DAY,
      reason: 'No working hours configured',
      isClickable: false
    };
  }
  
  // Non-working day (e.g., Saturday, Sunday)
  if (!config.isWorkingDay) {
    return {
      status: TimeSlotStatus.NON_WORKING_DAY,
      reason: 'Day off',
      isClickable: false
    };
  }
  
  // Before work hours
  if (timeInMinutes < config.workStart) {
    return {
      status: TimeSlotStatus.NON_WORKING,
      reason: `Before work hours (starts at ${minutesToTimeString(config.workStart)})`,
      isClickable: false
    };
  }
  
  // After work hours
  if (timeInMinutes >= config.workEnd) {
    return {
      status: TimeSlotStatus.NON_WORKING,
      reason: `After work hours (ends at ${minutesToTimeString(config.workEnd)})`,
      isClickable: false
    };
  }
  
  // During lunch break
  if (config.lunchStart !== null && config.lunchEnd !== null) {
    if (timeInMinutes >= config.lunchStart && timeInMinutes < config.lunchEnd) {
      return {
        status: TimeSlotStatus.LUNCH_BREAK,
        reason: `Lunch break (${minutesToTimeString(config.lunchStart)} - ${minutesToTimeString(config.lunchEnd)})`,
        isClickable: false
      };
    }
  }
  
  // Within working hours and not lunch
  return {
    status: TimeSlotStatus.AVAILABLE,
    reason: 'Available for interviews',
    isClickable: true
  };
}
```

---

## Step 5: Generate Time Slots for Calendar

### Create Time Slot Array

```typescript
interface TimeSlot {
  time: Date;
  timeLabel: string;      // "08:00 AM"
  status: TimeSlotStatus;
  reason: string;
  isClickable: boolean;
  cssClass: string;
}

/**
 * Generates time slots for a specific date
 * 
 * @param date - The date to generate slots for
 * @param workingHoursMap - Working hours configuration
 * @param intervalMinutes - Interval between slots (default: 30)
 * @param startHour - First hour to display (default: 0)
 * @param endHour - Last hour to display (default: 24)
 */
function generateTimeSlots(
  date: Date,
  workingHoursMap: Map<string, TimeSlotConfig>,
  intervalMinutes: number = 30,
  startHour: number = 0,
  endHour: number = 24
): TimeSlot[] {
  const slots: TimeSlot[] = [];
  const startMinutes = startHour * 60;
  const endMinutes = endHour * 60;
  
  for (let minutes = startMinutes; minutes < endMinutes; minutes += intervalMinutes) {
    const slotTime = new Date(date);
    slotTime.setHours(Math.floor(minutes / 60), minutes % 60, 0, 0);
    
    const statusResult = getTimeSlotStatus(date, minutes, workingHoursMap);
    
    slots.push({
      time: slotTime,
      timeLabel: slotTime.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: true 
      }),
      status: statusResult.status,
      reason: statusResult.reason || '',
      isClickable: statusResult.isClickable,
      cssClass: getStatusCssClass(statusResult.status)
    });
  }
  
  return slots;
}

/**
 * Maps status to CSS class
 */
function getStatusCssClass(status: TimeSlotStatus): string {
  switch (status) {
    case TimeSlotStatus.AVAILABLE:
      return 'time-slot-available';
    case TimeSlotStatus.LUNCH_BREAK:
      return 'time-slot-lunch';
    case TimeSlotStatus.NON_WORKING:
      return 'time-slot-non-working';
    case TimeSlotStatus.NON_WORKING_DAY:
      return 'time-slot-off-day';
    default:
      return 'time-slot-unknown';
  }
}
```

---

## Step 6: React Component Implementation

### Complete Calendar Day View Component

```tsx
import React, { useState, useEffect } from 'react';
import './CalendarDayView.css';

interface CalendarDayViewProps {
  selectedDate: Date;
  onTimeSlotClick?: (slot: TimeSlot) => void;
}

const CalendarDayView: React.FC<CalendarDayViewProps> = ({ 
  selectedDate, 
  onTimeSlotClick 
}) => {
  const [workingHoursMap, setWorkingHoursMap] = useState<Map<string, TimeSlotConfig>>(new Map());
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch working hours on component mount
  useEffect(() => {
    const loadWorkingHours = async () => {
      try {
        setLoading(true);
        const workingHours = await fetchWorkingHours();
        const map = buildWorkingHoursMap(workingHours);
        setWorkingHoursMap(map);
        setError(null);
      } catch (err) {
        console.error('Failed to load working hours:', err);
        setError('Failed to load working hours configuration');
      } finally {
        setLoading(false);
      }
    };

    loadWorkingHours();
  }, []);

  // Generate time slots when date or working hours change
  useEffect(() => {
    if (workingHoursMap.size > 0) {
      const slots = generateTimeSlots(
        selectedDate,
        workingHoursMap,
        30,  // 30-minute intervals
        6,   // Start at 6 AM
        22   // End at 10 PM
      );
      setTimeSlots(slots);
    }
  }, [selectedDate, workingHoursMap]);

  const handleSlotClick = (slot: TimeSlot) => {
    if (slot.isClickable && onTimeSlotClick) {
      onTimeSlotClick(slot);
    }
  };

  if (loading) {
    return <div className="calendar-loading">Loading calendar...</div>;
  }

  if (error) {
    return <div className="calendar-error">{error}</div>;
  }

  return (
    <div className="calendar-day-view">
      <h2 className="calendar-date-header">
        {selectedDate.toLocaleDateString('en-US', { 
          weekday: 'long', 
          year: 'numeric', 
          month: 'long', 
          day: 'numeric' 
        })}
      </h2>
      
      <div className="time-slots-container">
        {timeSlots.map((slot, index) => (
          <div
            key={index}
            className={`time-slot ${slot.cssClass} ${slot.isClickable ? 'clickable' : 'disabled'}`}
            onClick={() => handleSlotClick(slot)}
            title={slot.reason}
          >
            <span className="time-label">{slot.timeLabel}</span>
            {!slot.isClickable && (
              <span className="time-status-badge">
                {slot.status === TimeSlotStatus.LUNCH_BREAK ? '🍽️ Lunch' : 
                 slot.status === TimeSlotStatus.NON_WORKING_DAY ? '🏖️ Off' : 
                 '🚫 Closed'}
              </span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default CalendarDayView;
```

---

## Step 7: CSS Styling

### CalendarDayView.css

```css
.calendar-day-view {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.calendar-date-header {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.time-slots-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* Base time slot styles */
.time-slot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  position: relative;
  min-height: 50px;
}

.time-label {
  font-weight: 500;
  font-size: 14px;
}

.time-status-badge {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.1);
}

/* ✅ AVAILABLE - Green/White - Clickable */
.time-slot-available {
  background: #ffffff;
  border-color: #e0e0e0;
  color: #333;
  cursor: pointer;
}

.time-slot-available:hover {
  background: #e8f5e9;
  border-color: #4caf50;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(76, 175, 80, 0.2);
}

.time-slot-available.clickable {
  cursor: pointer;
}

/* ⚠️ LUNCH BREAK - Yellow/Orange - Not Clickable */
.time-slot-lunch {
  background: #fff3e0;
  border-color: #ffb74d;
  color: #e65100;
  cursor: not-allowed;
  opacity: 0.8;
}

.time-slot-lunch .time-label {
  color: #f57c00;
}

/* ❌ NON-WORKING HOURS - Gray - Disabled */
.time-slot-non-working {
  background: #f5f5f5;
  border-color: #e0e0e0;
  color: #9e9e9e;
  cursor: not-allowed;
  opacity: 0.6;
}

.time-slot-non-working .time-label {
  color: #bdbdbd;
  text-decoration: line-through;
}

/* 🏖️ NON-WORKING DAY - Dark Gray - Entire Day Off */
.time-slot-off-day {
  background: #eeeeee;
  border-color: #bdbdbd;
  color: #757575;
  cursor: not-allowed;
  opacity: 0.5;
}

.time-slot-off-day .time-label {
  color: #9e9e9e;
  text-decoration: line-through;
}

/* Disabled state */
.time-slot.disabled {
  cursor: not-allowed;
  pointer-events: none;
}

/* Loading and error states */
.calendar-loading,
.calendar-error {
  text-align: center;
  padding: 40px;
  font-size: 16px;
  color: #666;
}

.calendar-error {
  color: #d32f2f;
  background: #ffebee;
  border-radius: 8px;
  border: 1px solid #ef9a9a;
}

/* Responsive */
@media (max-width: 768px) {
  .calendar-day-view {
    padding: 10px;
  }
  
  .time-slot {
    padding: 10px 12px;
    min-height: 45px;
  }
  
  .time-label {
    font-size: 13px;
  }
  
  .time-status-badge {
    font-size: 11px;
  }
}
```

---

## Step 8: Usage Example

### In Your App

```tsx
import React, { useState } from 'react';
import CalendarDayView from './components/CalendarDayView';

function App() {
  const [selectedDate, setSelectedDate] = useState(new Date());

  const handleTimeSlotClick = (slot: TimeSlot) => {
    console.log('Selected time slot:', slot);
    alert(`You selected ${slot.timeLabel} on ${slot.time.toDateString()}`);
    // Handle interview scheduling, etc.
  };

  return (
    <div className="app">
      <h1>Interview Scheduler</h1>
      
      <div className="date-picker">
        <input
          type="date"
          value={selectedDate.toISOString().split('T')[0]}
          onChange={(e) => setSelectedDate(new Date(e.target.value))}
        />
      </div>
      
      <CalendarDayView
        selectedDate={selectedDate}
        onTimeSlotClick={handleTimeSlotClick}
      />
    </div>
  );
}

export default App;
```

---

## Visual Examples

### Example 1: Regular Working Day (Monday 8 AM - 5 PM)

```
06:00 AM ░░░░ Gray (Before work hours)
06:30 AM ░░░░ Gray (Before work hours)
07:00 AM ░░░░ Gray (Before work hours)
07:30 AM ░░░░ Gray (Before work hours)
08:00 AM ✅ Available (Clickable)
08:30 AM ✅ Available (Clickable)
09:00 AM ✅ Available (Clickable)
...
11:30 AM ✅ Available (Clickable)
12:00 PM 🍽️ Lunch Break (Orange, not clickable)
12:30 PM 🍽️ Lunch Break (Orange, not clickable)
01:00 PM ✅ Available (Clickable)
01:30 PM ✅ Available (Clickable)
...
04:30 PM ✅ Available (Clickable)
05:00 PM ░░░░ Gray (After work hours)
05:30 PM ░░░░ Gray (After work hours)
06:00 PM ░░░░ Gray (After work hours)
...
```

### Example 2: Non-Working Day (Saturday)

```
06:00 AM ███ Dark Gray (Day off)
06:30 AM ███ Dark Gray (Day off)
07:00 AM ███ Dark Gray (Day off)
...
12:00 PM ███ Dark Gray (Day off)
...
05:00 PM ███ Dark Gray (Day off)
...
```

---

## Advanced Features

### 1. Show Already Booked Slots

```typescript
interface TimeSlot {
  // ... existing fields
  isBooked?: boolean;
  bookingDetails?: {
    candidateName: string;
    interviewType: string;
  };
}

// Add to getTimeSlotStatus function
function getTimeSlotStatus(
  date: Date,
  timeInMinutes: number,
  workingHoursMap: Map<string, TimeSlotConfig>,
  bookedSlots: Set<number>  // ← New parameter
): TimeSlotStatusResult {
  // ... existing logic
  
  // Check if slot is already booked
  if (bookedSlots.has(timeInMinutes)) {
    return {
      status: TimeSlotStatus.BOOKED,
      reason: 'Time slot already booked',
      isClickable: false
    };
  }
  
  // ... rest of logic
}
```

### 2. Highlight Current Time

```tsx
const getCurrentTimeInMinutes = () => {
  const now = new Date();
  return now.getHours() * 60 + now.getMinutes();
};

// In component
const isCurrentTime = (slot: TimeSlot) => {
  const slotMinutes = slot.time.getHours() * 60 + slot.time.getMinutes();
  const currentMinutes = getCurrentTimeInMinutes();
  return Math.abs(slotMinutes - currentMinutes) < 30; // Within 30 minutes
};

// In JSX
<div className={`time-slot ${slot.cssClass} ${isCurrentTime(slot) ? 'current-time' : ''}`}>
```

### 3. Custom Interval Support

```typescript
// 15-minute intervals
const slots15min = generateTimeSlots(date, workingHoursMap, 15, 8, 18);

// 60-minute intervals
const slots60min = generateTimeSlots(date, workingHoursMap, 60, 8, 18);

// Show only working hours (8 AM - 5 PM)
const workHoursOnly = generateTimeSlots(date, workingHoursMap, 30, 8, 17);
```

---

## Testing Checklist

- [ ] Gray out appears correctly for times before work hours
- [ ] Gray out appears correctly for times after work hours
- [ ] Lunch break shows different color (orange/yellow)
- [ ] Non-working days (Saturday/Sunday) are fully grayed out
- [ ] Clickable time slots respond to hover effects
- [ ] Non-clickable slots show cursor: not-allowed
- [ ] Tooltip shows reason when hovering over slots
- [ ] Component updates when date changes
- [ ] Component updates when working hours are modified
- [ ] Responsive design works on mobile devices

---

## Troubleshooting

### Issue: All slots are gray
**Cause**: Working hours not loaded or date doesn't have configuration  
**Solution**: Check API response, verify JWT token, ensure working hours are set

### Issue: Gray out not appearing
**Cause**: Time comparison logic incorrect  
**Solution**: Verify `timeStringToMinutes()` function, check timezone handling

### Issue: Lunch break not showing
**Cause**: Lunch break times are null or not configured  
**Solution**: Set lunch break times in working hours configuration

### Issue: Wrong day of week
**Cause**: JavaScript Date.getDay() returns 0-6 (Sunday-Saturday)  
**Solution**: Verify `getDayOfWeek()` function maps correctly to API format

---

## Complete API Integration Summary

| API Endpoint | Purpose | Usage |
|--------------|---------|-------|
| `GET /api/v1/interview-calendar/recruiters/working-hours` | Get all working hours | Fetch once on load, cache locally |
| `POST /api/v1/interview-calendar/recruiters/working-hours` | Set single day | Update specific day configuration |
| `POST /api/v1/interview-calendar/recruiters/working-hours/batch` | Set multiple days | Initial setup or bulk update |

---

## Performance Optimization

### 1. Cache Working Hours

```typescript
// Cache for 5 minutes
const CACHE_DURATION = 5 * 60 * 1000;

class WorkingHoursCache {
  private cache: WorkingHoursResponse[] | null = null;
  private cacheTime: number = 0;

  async get(): Promise<WorkingHoursResponse[]> {
    const now = Date.now();
    if (this.cache && (now - this.cacheTime) < CACHE_DURATION) {
      return this.cache;
    }
    
    this.cache = await fetchWorkingHours();
    this.cacheTime = now;
    return this.cache;
  }

  invalidate() {
    this.cache = null;
    this.cacheTime = 0;
  }
}
```

### 2. Memoize Time Slots

```tsx
import { useMemo } from 'react';

const timeSlots = useMemo(() => {
  return generateTimeSlots(selectedDate, workingHoursMap, 30, 6, 22);
}, [selectedDate, workingHoursMap]);
```

---

## Summary

✅ **Fetch** working hours from API  
✅ **Parse** time strings to minutes  
✅ **Determine** status for each time slot  
✅ **Apply** CSS classes based on status  
✅ **Gray out** non-working hours  
✅ **Highlight** lunch breaks  
✅ **Enable** clickable slots only during work hours  
✅ **Show** tooltips with reasons  

**Key Points:**
- Backend provides working hours via GET API
- Frontend calculates gray-out based on `startTime` and `endTime`
- Lunch breaks get special styling (orange/yellow)
- Non-working days are fully disabled
- Only available slots are clickable

**Last Updated**: November 27, 2025
