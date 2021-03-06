package club.zabavy.core.service.impl;

import club.zabavy.core.dao.GamingDayDAO;
import club.zabavy.core.domain.entity.GamingDay;
import club.zabavy.core.service.GamingDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class GamingDayServiceImpl implements GamingDayService {

	@Autowired
	GamingDayDAO gamingDayDAO;

	@Override
	public List<GamingDay> findByParam(Date dateFrom, Date dateTo,int offset, int limit) {
		return gamingDayDAO.findByParam(dateFrom, dateTo, offset, limit);
	}

	@Override
	public List<Object> getEventsCount(Date dateFrom, Date dateTo) {
		return gamingDayDAO.getEventsCount(dateFrom, dateTo);
	}

	@Override
	public GamingDay findById(long id) {
		return gamingDayDAO.findById(id);
	}

	@Override
	public void insert(GamingDay gamingDay) {
		gamingDayDAO.insert(gamingDay);
	}

	@Override
	public GamingDay update(GamingDay gamingDay) {
		if(gamingDay.getId() <= 0) throw new NullPointerException("Wrong id.");
		return gamingDayDAO.update(gamingDay);
	}

	@Override
	public void remove(long id) {
		if(id <= 0) throw new NullPointerException("Wrong id.");
		gamingDayDAO.remove(id);
	}

}
