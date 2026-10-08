package com.simpleengine.core;

public final class EngineSession {
    private final String sessionId;
    private final String username;
    private final String versionId;

    public EngineSession(String sessionId, String username, String versionId) {
        this.sessionId = sessionId;
        this.username = username;
        this.versionId = versionId;
    }

    public String getSessionId() { return sessionId; }
    public String getUsername() { return username; }
    public String getVersionId() { return versionId; }
}
