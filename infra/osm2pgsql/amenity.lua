-- Script Lua để osm2pgsql nạp các loại tiện ích từ OSM vào bảng amenity
-- Dùng với: osm2pgsql --output=flex --style=infra/osm2pgsql/amenity.lua
-- Tham khảo: https://osm2pgsql.org/doc/manual.html#the-flex-output

local tables = {}

-- Bảng đích trong PostgreSQL
tables.amenity = osm2pgsql.define_node_table('amenity_import', {
    { column = 'osm_id',      type = 'bigint' },
    { column = 'category',    type = 'text' },
    { column = 'subcategory', type = 'text' },
    { column = 'name',        type = 'text' },
    { column = 'geom',        type = 'point', projection = 4326 },
})

-- Ánh xạ tag OSM → category/subcategory
local category_map = {
    -- Bệnh viện / phòng khám
    { amenity = 'hospital',   category = 'hospital',    subcategory = nil },
    { amenity = 'clinic',     category = 'clinic',      subcategory = nil },
    { amenity = 'doctors',    category = 'clinic',      subcategory = 'doctors' },
    -- Trường học
    { amenity = 'school',     category = 'school',      subcategory = nil },
    { amenity = 'university', category = 'school',      subcategory = 'university' },
    { amenity = 'college',    category = 'school',      subcategory = 'college' },
    { amenity = 'kindergarten', category = 'school',    subcategory = 'kindergarten' },
    -- Chợ / siêu thị
    { shop = 'supermarket',   category = 'supermarket', subcategory = nil },
    { shop = 'convenience',   category = 'market',      subcategory = 'convenience' },
    { amenity = 'marketplace',category = 'market',      subcategory = nil },
}

-- Tạo lookup table để tìm nhanh
local amenity_lookup = {}
local shop_lookup = {}
for _, m in ipairs(category_map) do
    if m.amenity then
        amenity_lookup[m.amenity] = { category = m.category, subcategory = m.subcategory }
    end
    if m.shop then
        shop_lookup[m.shop] = { category = m.category, subcategory = m.subcategory }
    end
end

function osm2pgsql.process_node(object)
    local tags = object.tags
    local match = nil

    if tags.amenity then
        match = amenity_lookup[tags.amenity]
    elseif tags.shop then
        match = shop_lookup[tags.shop]
    end

    if not match then return end

    tables.amenity:insert({
        osm_id      = object.id,
        category    = match.category,
        subcategory = match.subcategory,
        name        = tags.name or tags['name:vi'],
        geom        = object:as_point(),
    })
end

-- Bỏ qua way/relation cho tiện ích dạng point
function osm2pgsql.process_way(object) end
function osm2pgsql.process_relation(object) end

