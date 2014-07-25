package club.zabavy.core.controller;

import club.zabavy.core.domain.entity.Meeting;
import club.zabavy.core.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class MeetingController {

	@Autowired
	MeetingService meetingService;

	@RequestMapping(value = "/meetings", method = RequestMethod.GET)
	@ResponseBody
	public List<Meeting> getMeetings() {
		return meetingService.getAll();
	}

	@RequestMapping(value = "/meetings", method = RequestMethod.POST)
	@ResponseBody
	public Meeting saveMeeting(@RequestBody Meeting meeting) {
		meetingService.insert(meeting);
		return meeting;
	}

	@RequestMapping(value = "/meetings/{meetingId}", method = RequestMethod.GET)
	@ResponseBody
	public Meeting getMeetingById(@PathVariable("meetingId") Long id, HttpServletResponse response) throws IOException {
		Meeting meeting = meetingService.findById(id);
		if(meeting == null) response.sendError(404);
		return meeting;
	}

	@RequestMapping(value = "/meetings/{meetingId}", method = RequestMethod.PUT)
	@ResponseBody
	public void updateMeeting(@PathVariable("meetingId") Long id, @RequestBody Meeting meeting) {
		meeting.setId(id);
		meetingService.update(meeting);
	}

	@RequestMapping(value = "/meetings/{meetingId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteMeeting(@PathVariable("meetingId") Long id) {
		meetingService.remove(id);
	}
}
