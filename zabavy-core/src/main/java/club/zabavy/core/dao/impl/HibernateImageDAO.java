package club.zabavy.core.dao.impl;

import club.zabavy.core.dao.ImageDAO;
import club.zabavy.core.domain.entity.Image;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
@SuppressWarnings("JpaQlInspection")
public class HibernateImageDAO implements ImageDAO{

	@Autowired
	SessionFactory sessionFactory;

	@Override
	public List<Image> getAll() {
		Query query = sessionFactory.getCurrentSession().createQuery("from Image");
		return query.list();
	}

	@Override
	public Image findById(long id) {
		return (Image) sessionFactory.getCurrentSession().get(Image.class, id);
	}

	@Override
	public void insert(Image image) {
		sessionFactory.getCurrentSession().save(image);
	}

	@Override
	public Image update(Image image) {
		Image i = findById(image.getId());

		if(image.getUrl() != null) i.setUrl(image.getUrl());

		i.setUpdatedAt(new Date());
		sessionFactory.getCurrentSession().update(i);
		return i;
	}

	@Override
	public void remove(long id) {
		Session session = sessionFactory.getCurrentSession();
		Image image = findById(id);
		if(image != null) session.delete(image);
	}
}
