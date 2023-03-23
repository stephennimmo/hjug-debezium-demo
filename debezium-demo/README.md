# Let's Play with Debezium

In this demo, we will setup the infrastructure for debezium and demonstrate the basic capabilities as well as toggling a few of the configuration propeties to demonstrate the changes in the output. 

# Prerequisities

You need a local container runtime environment. I recommend Podman. https://podman.io  

I'm on a Mac, so I use brew. 

```
brew install podman
brew install podman-compose
```

or you can just install Podman Desktop. https://podman-desktop.io  

```
brew install --cask podman-desktop
```

We also are going to use the Kafka Connect CLI. https://github.com/kcctl/kcctl  

```
brew install kcctl/tap/kcctl
```

Once that's installed, you will need to set the context for our local environment. 

```
kcctl config set-context local --cluster=http://localhost:8083
```

# Start the Environment

Four console tabs needed:
* podman-compose
* kcctl
* kafka
* psql

We are going to start a Podman compose file containing the following: 
* PostgreSQL instance initialized with the `postgres-init.sql` file. 
* Single Kafka instance using the new Zookeeper-less version
* Instance of the Kafka connect runtime  

To get this started, use the following command: 

```
podman-compose up
```

Notice we didn't add the `-d` argument. We want to keep an eye on what is happening. 

# Create the Debezium Kafka Connector

Now we need to add the Kafka connector definition for us to connect to the postgres instance and listen to some tables and the transaction logs. 

```
kcctl apply -f etrm-connector.json
```

Any time you reapply the connector config, you have to restart the connector.  
```
kcctl restart connector etrm-connector
```

### Talking Points
* The "topic.creation.enable" configurations and empty tables, no transaction topic

# Open the Consoles to Watch

As we interact with the database tables, we are going to want to watch what's being emitted by Debezium. For that, we need to bash into the kafka pod and use the Kafka client tools we already have there. 

```
podman exec -it kafka /bin/bash
```

Need to create the transaction topic
```
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic etrm.transaction --partitions 3 --if-not-exists
```

```
bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```
```
bin/kafka-topics.sh --bootstrap-server localhost:9092 --topic etrm.* --describe
```
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --whitelist '.*etrm.*'  --property print.key=true

# --from-beginning 
```

# Open Database Console and Tests

```
podman exec -it postgres /bin/bash
```
```
psql -d etrm -U admin
```

Insert into just trade header
```
insert into trade_header (start_date, end_date, execution_timestamp, trade_type_id) VALUES (CURRENT_DATE - 30, CURRENT_DATE + 5, CURRENT_TIMESTAMP, 1);
```

Insert into trade_header and trade_leg in single transaction (most likely)
```
BEGIN;
WITH new_trade AS (
    insert into trade_header (start_date, end_date, execution_timestamp, trade_type_id) VALUES (CURRENT_DATE - 30, CURRENT_DATE + 5, CURRENT_TIMESTAMP, 1)
        RETURNING trade_id
)

INSERT INTO trade_leg (trade_id, payer_id, receiver_id, commodity_id, location_id, price, price_currency_id, quantity, quantity_uom_id)
VALUES ((select trade_id from new_trade), 1, 2, 1, 1, 2.84, 1, 10000, 1),
       ((select trade_id from new_trade), 2, 1, 1, 1, 2.84, 1, 10000, 1);
COMMIT;
```

```
UPDATE trade_leg SET price = 3.85 WHERE trade_leg_id = 110;
```


# Cleanup
```
podman-compose down
```

# Links

https://hub.docker.com/_/postgres

# Backlog

JsonConverter
https://gist.githubusercontent.com/yildirimabdullah/eb54f3386a37acbcb07f68c1bb13a15e/raw/a928622039ffb08284bba8674b52c1eb2fc1a653/create-connector.sh

We were producing JSON messages with schemas enabled; this creates larger Kafka records than needed, in particular if schema changes are rare. Hence we decided to disable message schemas by setting key.converter.schemas.enabled and value.converter.schemas.enabled to false to reduce the size of each payload considerably hence saving on network bandwidth and serialization/deserialization costs. The only downside is that we now need to maintain the schema of those messages in an external schema registry.
https://debezium.io/blog/2020/02/25/lessons-learned-running-debezium-with-postgresql-on-rds/


https://debezium.io/documentation/reference/stable/operations/debezium-ui.html