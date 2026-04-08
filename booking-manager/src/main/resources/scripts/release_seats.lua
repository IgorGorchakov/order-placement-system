-- KEYS[1]: availability key (trip-availability:{tripId})
-- KEYS[2..n]: seat-lock:{tripId}:{seat1}, seat-lock:{tripId}:{seat2}, ...
-- ARGV[1]: number of seats to release

-- Step 1: Delete all seat lock keys
for i = 2, #KEYS do
    redis.call('DEL', KEYS[i])
end

-- Step 2: Increment availability (protects against going negative by checking first)
local currentAvailability = tonumber(redis.call('GET', KEYS[1]) or '0')
local seatsToRelease = tonumber(ARGV[1])

-- Increment the availability counter
local newCount = redis.call('INCRBY', KEYS[1], seatsToRelease)

-- Return the new availability count
return newCount
