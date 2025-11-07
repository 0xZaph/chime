local rateLimitkey = KEYS[1]
local attemptCountKey = KEYS[2]

if redis.call('EXISTS', rateLimitkey) == 1 then
    local ttl = redis.call('TTL', rateLimitkey)
    return { -1, ttl > 0 and ttl or 60 }
end

local currentCount = redis.call('GET', attemptCountKey)
currentCount = currentCount and tonumber(currentCount) or 0

local newCount = redis.call('INCR', attemptCountKey)

local backOffSeconds = { 60, 300, 3600 }
local backOffIndex = math.min(currentCount, 2) + 1

redis.call('SETEX', rateLimitkey, backOffSeconds[backOffIndex], '1')

redis.call('EXPIRE', attemptCountKey, 86400)

return { newCount, 0}