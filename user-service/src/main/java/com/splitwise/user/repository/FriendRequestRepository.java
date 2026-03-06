package com.splitwise.user.repository;

import com.splitwise.user.model.FriendRequest;
import com.splitwise.user.model.FriendRequestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {

    Optional<FriendRequest> findBySenderIdAndReceiverIdAndStatus(String senderId, String receiverId, FriendRequestStatus status);

    Optional<FriendRequest> findBySenderIdAndReceiverId(String senderId, String receiverId);

    List<FriendRequest> findByReceiverIdAndStatus(String receiverId, FriendRequestStatus status);

    List<FriendRequest> findBySenderIdAndStatus(String senderId, FriendRequestStatus status);

    Optional<FriendRequest> findByToken(String token);
}
