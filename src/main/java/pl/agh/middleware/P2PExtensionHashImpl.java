package pl.agh.middleware;

import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.base.JoinToNetworkRequest;
import pl.agh.p2pnetwork.ports.outbound.P2PExtension;

public class P2PExtensionHashImpl implements P2PExtension {
    @Override
    public BaseMessage additionalActionOnNodeJoinToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        return P2PExtension.super.additionalActionOnNodeJoinToNetwork(joinToNetworkRequest);
        //TODO (14.01.2025): Dorobić tutaj tworzenie dumpoa z bazy danych o naszych taskach

//        Taski i batche

        // In my implementation
        //        objectMapper.writeValueAsString(taskController.getMemoryDump());
    }

    @Override
    public BaseMessage handleMessageOverP2P(BaseMessage baseMessage) {
        return P2PExtension.super.handleMessageOverP2P(baseMessage);
    }
}


//                    case NewTaskRequest newTaskRequest -> {
//                        UUID taskId = handleNewTaskRequest(newTaskRequest);
//                        out.println(taskId.toString());
//                    }
//    private void handleMemoryDumpMessage(MemoryDumpMessage memoryDumpMessage) {
//        logger.info("Handle memory dump message");
//        logger.info("Received tasks: " + memoryDumpMessage.getTasksFromNetworkMessages().size());
//        logger.info("Received batches: " + memoryDumpMessage.getBatchUpdateDtoList().size());
//        memoryDumpMessage.getTasksFromNetworkMessages().forEach(task ->
//                handleNewTaskFromNetwork(task));
//        memoryDumpMessage.getBatchUpdateDtoList().forEach(batch ->
//                handleBatchUpdateMessage(new BatchUpdateMessage(batch)));
//    }
//
//    private void handleUpdateNetworkMessage(UpdateNetworkMessage updateMessage) {
//        logger.info("Handle update network message - Add new nodes: " + updateMessage.getNodes());
//        networkManager.updateNetwork(updateMessage);
//    }
//
//    private UUID handleNewTaskRequest(NewTaskRequest newTaskRequest) {
//        logger.info("TCPListener.handleNewTaskRequest - invoked");
//        UUID taskId = taskController.createNewTask(TaskMapper.toDto(newTaskRequest));
//        logger.info("New task id: " + taskId);
//        return taskId;
//    }
//
//    private void handleNewTaskFromNetwork(TaskUpdateMessage newTaskRequestFromNetwork) {
//        try {
//            logger.info("Handle new task from network " + newTaskRequestFromNetwork.getTaskId());
//
//            if(newTaskRequestFromNetwork.getTaskId() != null) {
//                logger.info("Zapis wyniku taska. TaskId: " + newTaskRequestFromNetwork.getTaskId());
//                doneTaskProcessor.processDoneTask(newTaskRequestFromNetwork.getTask());
//            } else {
//                UUID taskId = taskController.createNewTaskFromNetwork(newTaskRequestFromNetwork);
//
//                logger.info("Start new task from network");
//                taskController.startTask(taskId);
//
//                logger.info("Pomyślnie obsłużono nowe zadanie z sieci: " + taskId);
//            }
//        } catch (Exception e) {
//            logger.error("Błąd podczas obsługi nowego zadania z sieci: " + e.getMessage());
//        }
//    }
//
//    private void handleBatchUpdateMessage(BatchUpdateMessage newBatchUpdateMessage) {
//        logger.info("Handle batch update message");
//        taskController.receiveBatchUpdateMessage(BatchMapper.messageToBatchUpdateDto(newBatchUpdateMessage));
//    }
