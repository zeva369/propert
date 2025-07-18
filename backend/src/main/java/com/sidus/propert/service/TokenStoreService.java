package com.sidus.propert.service;

public interface TokenStoreService {

    /** * Checks if a token exists for the given user ID.
     *
     * @param userID the ID of the user whose token existence is to be checked
     * @return true if a token exists for the user, false otherwise
     */
    Boolean exists(String userID);

    /**
     * Retrieves the token associated with the given user ID.
     *
     * @param userId the ID of the user whose token is to be retrieved
     * @return the token as a String, or null if no token is found
     */
    String getToken(String userId);

    /**
     * Stores a token for the given user ID.
     *
     * @param userId the ID of the user for whom the token is to be stored
     * @param token  the token to be stored
     */
    void storeToken(String userId, String token);

    /**
     * Deletes the token associated with the given user ID.
     *
     * @param userId the ID of the user whose token is to be deleted
     */
    void delete(String userId);
}
