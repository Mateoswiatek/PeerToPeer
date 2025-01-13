#echo '{"type": "NewTaskRequest", "passwordHash": "89420ef764ba98728f16a21c5da122f25fdc936b535bbf0d5ccf1ba0ec6dab3b", "alphabet": "abcdoijklms", "maxLength": 5, "maxBatchSize": 40}' | nc localhost 5000


echo '{"type": "NewTaskRequest", "passwordHash": "216a4438875df831967fc4c6c2b15469a4c6f62dc4d28a2b5cddebddf4cfe5ad", "alphabet": "abcdoijklms", "maxLength": 10, "maxBatchSize": 500}' | nc localhost 5000
