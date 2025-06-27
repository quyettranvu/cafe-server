SELECT item_name,
       SUM(o.quantity)           AS total_quantity,
       SUM(oi.quantity * oi.price)   AS total_revenue
FROM   order_item  oi
JOIN   orders       o  ON oi.order_id = o.id
WHERE  o.order_time >= ?           -- 1st placeholder
  AND  o.order_time <  ?           -- 2nd placeholder
GROUP BY item_name