package backend;

import model.Alert;
import java.util.List;

public interface AlertStorage {
    void save(Alert alert);
    List<Alert> loadAll();
    void clear();
}
