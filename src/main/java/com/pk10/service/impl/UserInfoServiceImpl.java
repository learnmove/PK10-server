package com.pk10.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pk10.bean.*;
import com.pk10.dao.LotteryHistoryDao;
import com.pk10.dao.UserBetDao;
import com.pk10.dao.UserInfoDao;
import com.pk10.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserInfoServiceImpl implements UserInfoService {

	private static final Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

	@Autowired
	private UserInfoDao userInfoDao;
	@Autowired
	private UserBetDao userBetDao;
	@Autowired
	private TokenConfig tokenConfig;

	@Autowired
	private LotteryHistoryDao lotteryHistoryDao;

	@Override
	public Integer save(UserInfo t) throws Exception {
		if (userInfoDao.update(t) == 0) {
			return userInfoDao.save(t);
		} else {
			return 0;
		}
	}

	@Override
	public Integer update(UserInfo t) throws Exception {
		return userInfoDao.update(t);
	}

	@Override
	public List<UserInfo> getAll() throws Exception {
		return userInfoDao.getAll();
	}

	@Override
	public UserInfo getOneById(UserInfo t) throws Exception {
		UserInfo userInfo = userInfoDao.getOneById(t);
		if (userInfo == null) {
			t.setMoney(tokenConfig.getMoney());// 如果用户第一次登录则赠送 默认数的金币
			userInfoDao.save(t);
			return t;
		} else {
			return userInfo;
		}
	}

	@Override
	public Integer deleteOneById(UserInfo t) throws Exception {
		return userInfoDao.deleteOneById(t);
	}

	@Override
	public UserInfo cashPrize(UserInfo userInfo) throws Exception {
		UserInfo safeUserInfo = userInfoDao.getOneById(userInfo);
		List<UserBet> userBets = userBetDao.getUnCashPrize(safeUserInfo);
		for (UserBet userBet : userBets) {
			// 获取档期开奖结果 如果获取不到说明还没有从网上下载下来，则等待3秒钟再去查询
			LotteryHistory lotteryHistory = null;
			// for (int i = 0; i < 30; i++) {
			lotteryHistory = lotteryHistoryDao.getOneById(new LotteryHistory(userBet.getIdnum(), null, null));
			// if (lotteryHistory == null) {
			// Thread.sleep(3000);
			// continue;
			// } else {
			// break;
			// }
			// }
			// 如果60秒后还是查不到 则放弃
			if (lotteryHistory == null) {
				continue;
				// throw new Exception("get bouns result error");
			}
			String[] split = lotteryHistory.getLotterynums().split(",");
			Integer lotterynum = (Integer.parseInt(split[0]) + Integer.parseInt(split[split.length - 1])) % 10; // 计算中奖号码
			switch (userBet.getType()) {
			case NUMBER:
				if (Integer.parseInt(userBet.getBetnum()) == lotterynum) { // 中奖了
					cashUpdateUser(safeUserInfo, userBet);
				} else { // 未中奖
					userBet.setResult("0");
					userBet.setCrashbalance(userBet.getBalance());
					userBetDao.update(userBet); // 重置兑奖标志位
				}
				break;
			case SINGLE_OR_DOUBLE:
				if ("single".equals(userBet.getBetnum()) && lotterynum % 2 == 1) {
					cashUpdateUser(safeUserInfo, userBet);
				} else if ("double".equals(userBet.getBetnum()) && lotterynum % 2 == 0) {
					cashUpdateUser(safeUserInfo, userBet);
				} else {
					userBet.setCrashbalance(userBet.getBalance());
					userBet.setResult("0");
					userBetDao.update(userBet);
				}
				break;
			case BIG_OR_SMALL:
				if ("big".equals(userBet.getBetnum()) && lotterynum >= 5) {
					cashUpdateUser(safeUserInfo, userBet);
				} else if ("small".equals(userBet.getBetnum()) && lotterynum < 5) {
					cashUpdateUser(safeUserInfo, userBet);
				} else {
					userBet.setCrashbalance(userBet.getBalance());
					userBet.setResult("0");
					userBetDao.update(userBet);
				}
				break;
			default:
				break;
			}
		}
		return safeUserInfo;
	}

	private void cashUpdateUser(UserInfo safeUserInfo, UserBet userBet) throws Exception {
		logger.info("bonus number:" + userBet.getBetnum() + "cash a prizeing....");
		safeUserInfo.setMoney(safeUserInfo.getMoney() + (int) (userBet.getBetmoney() * userBet.getOdds()));
		userBet.setCrashbalance(safeUserInfo.getMoney() + (int) (userBet.getBetmoney() * userBet.getOdds()));
		userBet.setResult("+" + (int) (userBet.getBetmoney() * userBet.getOdds()));
		userInfoDao.update(safeUserInfo);
		userBetDao.update(userBet);// 兑奖后重置标志位表示已兑奖
		logger.info("bonus number:" + userBet.getBetnum() + "cash a prizeing....");
	}

	@Override
	public UserInfo getUserInfoByTel(UserInfo userInfo) {
		return userInfoDao.getUserInfoByTel(userInfo);
	}

	@Override
	public UserInfo getUserInfoByUsername(UserInfo userInfo) {
		return userInfoDao.getUserInfoByUsername(userInfo);
	}

	@Override
	public UserInfo login(UserInfo userInfo) {
		return userInfoDao.login(userInfo);
	}

	@Override
	public UserInfo managerLogin(UserInfo userInfo) {
		return userInfoDao.managerLogin(userInfo);
	}

	@Override
	public Datagrid getAllAgent(Page page, AgentInfo agentInfo) {
		PageHelper.startPage(page.getPages(), 10);
		List<AgentInfo> agentInfos = userInfoDao.getAllAgent(agentInfo);
		PageInfo pageInfo = new PageInfo(agentInfos);
		Datagrid datagrid = new Datagrid();
		datagrid.setRows(agentInfos);
		datagrid.setTotal(pageInfo.getTotal());
		datagrid.setTotalPage(pageInfo.getPages());
		datagrid.setCurrentPage(page.getPages());
		return datagrid;
	}

	@Override
	public AgentInfo getAgentById(AgentInfo agentInfo) {
		return userInfoDao.getAgentById(agentInfo);
	}

	@Override
	public Integer updateAgent(AgentInfo agentInfo) {
		AgentInfo myAgentInfo = userInfoDao.getAgentById(agentInfo);
		myAgentInfo.setNickname(agentInfo.getNickname());
		myAgentInfo.setPassword(agentInfo.getPassword());
		myAgentInfo.setTel(agentInfo.getTel());
		myAgentInfo.setRebate(agentInfo.getRebate());
		myAgentInfo.setDetail(agentInfo.getDetail());
		return userInfoDao.updateAgentByPrimaryKeySelective(myAgentInfo);
	}

	@Override
	public Integer savaAgent(AgentInfo agentInfo) {
		return userInfoDao.saveAgent(agentInfo);
	}

	/**
	 * 查找指定代理商下的所有用户
	 * @param userInfo
	 * @return
	 */
	@Override
	public List<UserInfo> getUsersForAgent(UserInfo userInfo) {
		return userInfoDao.getUsersForAgent(userInfo);
	}

	@Override
	public List<UserInfo> getAgentsById(Integer id) {
		return userInfoDao.getAgentsById(id);
	}

	@Override
	public List<UserInfo> getAgentsByOwnerId(Integer ownerId) {
		return userInfoDao.getAgentsByOwnerId(ownerId);
	}

	@Override
	public List<UserInfo> getUsersByAgentId(String username, Integer agentId) {
		return userInfoDao.getUsersByAgentId(username, agentId);
	}

	public UserInfo getUserUsername(UserInfo userInfo) {
		return userInfoDao.getUserUsername(userInfo);
	}
}
