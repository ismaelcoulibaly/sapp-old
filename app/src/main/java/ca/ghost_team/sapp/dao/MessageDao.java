package ca.ghost_team.sapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ca.ghost_team.sapp.model.Message;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM MessageTable WHERE idReceiver = :idUser OR idSender = :idUser")
    LiveData<List<Message>> allMessages(int idUser);

    @Query("SELECT * FROM MessageTable WHERE idReceiver = :idUser GROUP BY annonceId")
    LiveData<List<Message>> allMessagesReceiver(int idUser);

    @Query("SELECT * FROM MessageTable WHERE ((idReceiver = :idUser AND idSender = :idSender) OR (idReceiver = :idSender AND idSender =:idUser)) AND annonceId = :idAnnonce")
    LiveData<List<Message>> allMessagesBetween(int idUser, int idSender, int idAnnonce);

    @Insert
    void sendMessage(Message message);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void sendMessage(Message... message);

    @Update
    void putRead(Message... message);

}
