package moon.mission.rescue.badapplication.entity;

import jakarta.persistence.Id;
import moon.mission.rescue.badapplication.model.Fleet;
import moon.mission.rescue.badapplication.model.StarShip;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;

@Document(collection = "fleets")
public class FleetDocument {

    @Id
    private String id;
    private List<StarShip> starships;

    public FleetDocument(List<StarShip> starships) {
        this.starships = starships;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<StarShip> getStarships() {
        return starships;
    }

    public void setStarships(List<StarShip> starships) {
        this.starships = starships;
    }

    public Fleet toDomain() {
        return new Fleet(id, starships);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FleetDocument that = (FleetDocument) o;
        return Objects.equals(id, that.id) && Objects.equals(starships, that.starships);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, starships);
    }
}
