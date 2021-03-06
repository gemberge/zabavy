package club.zabavy.core.dao;

import club.zabavy.core.domain.entity.GamingDay;

import java.util.Date;
import java.util.List;

public interface GamingDayDAO extends BaseDAO<GamingDay> {
	List<GamingDay> findByParam(Date dateFrom, Date dateTo, int offset, int limit);
	List<Object> getEventsCount(Date dateFrom, Date dateTo);
}
