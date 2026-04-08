-- KEYS[1..n]: seat-lock:{tripId}:{seat1}, seat-lock:{tripId}:{seat2}, ...
-- ARGV[1]: availability key (trip-availability:{tripId})
-- ARGV[2]: number of seats to lock
-- ARGV[3]: TTL in seconds

-- Step 1: Check all seats are available (not locked)
for i, key in ipairs(KEYS) do
    if redis.call('EXISTS', key) == 1 then
        -- Seat already locked, abort and return the conflicting seat index (negative)
        return -i
    end
end

-- Step 2: Check if we have enough seats available
local currentAvailability = tonumber(redis.call('GET', ARGV[1]) or '0')
local seatsToLock = tonumber(ARGV[2])

if currentAvailability < seatsToLock then
    -- Not enough seats available
    return -999
end

-- Step 3: Lock all seats atomically
for i, key in ipairs(KEYS) do
    redis.call('SET', key, 'locked', 'EX', ARGV[3])
end

-- Step 4: Decrement availability
local newCount = redis.call('DECRBY', ARGV[1], seatsToLock)

-- Return new availability count
return newCount
