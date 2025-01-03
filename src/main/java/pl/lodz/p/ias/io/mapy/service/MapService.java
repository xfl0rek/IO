package pl.lodz.p.ias.io.mapy.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.ias.io.mapy.model.MapPoint;
import pl.lodz.p.ias.io.mapy.repository.MapPointRepository;

import java.util.List;

@AllArgsConstructor
@Service
public class MapService implements IMapService {

    private MapPointRepository mapPointRepository;

    @Override
    public MapPoint addPoint(MapPoint point) {
        mapPointRepository.save(point);
        return point;
    }

//    @Override
//    public MapPoint getPoint(double x, double y) {
//        throw new UnsupportedOperationException("Not implemented yet");
//    }

    @Override
    public MapPoint getPoint(long id) {
        return mapPointRepository.findById(id).get();
    }

    @Override
    public List<MapPoint> getPoints() {
        List<MapPoint> a = mapPointRepository.findAll();
        return mapPointRepository.findAll();
    }

    @Override
    public void removePoint(long id) {
        mapPointRepository.delete(mapPointRepository.findById(id).get());
    }

    @Override
    public void changeStatus(long id, boolean status) {
//        mapPointRepository.changeStatus(mapPointRepository.findById(id), status);
    }
}
