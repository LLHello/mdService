local stockKey = KEYS[1]
local userKey = KEYS[2]
local metaKey = KEYS[3]
local userId = ARGV[1]
local nowMs = tonumber(ARGV[2])

local status = tonumber(redis.call('HGET', metaKey, 'status'))
if status == nil then
  return 3
end
if status ~= 1 then
  return 4
end

local startMs = tonumber(redis.call('HGET', metaKey, 'startMs'))
local endMs = tonumber(redis.call('HGET', metaKey, 'endMs'))
if startMs ~= nil and startMs > 0 and nowMs < startMs then
  return 5
end
if endMs ~= nil and endMs > 0 and nowMs > endMs then
  return 6
end

local exists = redis.call('SISMEMBER', userKey, userId)
if exists == 1 then
  return 2
end

local stock = tonumber(redis.call('GET', stockKey))
if stock == nil then
  return 3
end
if stock <= 0 then
  return 1
end

redis.call('DECR', stockKey)
redis.call('SADD', userKey, userId)
return 0
