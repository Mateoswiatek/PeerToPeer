# Solo 5000
java -jar target/P2PNetwork-1.0-SNAPSHOT.jar 5000

# New 5001 join to 5000
java -jar target/P2PNetwork-1.0-SNAPSHOT.jar 5001 localhost 5000

# New 5002 join to 5001 (After remove, interrupt the node at port 5000
java -jar target/P2PNetwork-1.0-SNAPSHOT.jar 5002 localhost 5001

# New 5003 join to 5001 (After complete the task by nodes 5001 and 5002)
java -jar target/P2PNetwork-1.0-SNAPSHOT.jar 5003 localhost 5001
