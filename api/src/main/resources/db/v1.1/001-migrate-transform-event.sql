-- @formatter:off
UPDATE photo_event
SET data = jsonb_set(data, '{content}', '{"type": "rotation", "degrees": 90.0}')
WHERE type = 'transform'
  AND data #>> '{content,type}' = 'rotation'
  AND data #>> '{content,direction}' = 'clockwise';

UPDATE photo_event
SET data = jsonb_set(data, '{content}', '{"type": "rotation", "degrees": -90.0}')
WHERE type = 'transform'
  AND data #>> '{content,type}' = 'rotation'
  AND data #>> '{content,direction}' = 'counterclockwise';
