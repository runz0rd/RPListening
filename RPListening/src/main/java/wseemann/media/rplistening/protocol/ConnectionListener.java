package wseemann.media.rplistening.protocol;

public interface ConnectionListener {
	void onConnected(PrivateListeningSession session);
    void onFailure(Throwable error);
}