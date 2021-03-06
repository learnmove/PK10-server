package com.pk10.dao;

import com.pk10.bean.BetInit;
import com.pk10.bean.BetInitExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BetInitMapper {
    int countByExample(BetInitExample example);

    int deleteByExample(BetInitExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(BetInit record);

    int insertSelective(BetInit record);

    List<BetInit> selectByExample(BetInitExample example);

    List<BetInit> selectByGameName(String gName);

    List<BetInit> selectAll();

    List<BetInit> selectAllGameName();

    List<BetInit> selectAllTypeByGName(String gName);

    BetInit selectByGnameAndGtype(BetInit betInit);

    BetInit selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") BetInit record, @Param("example") BetInitExample example);

    int updateByExample(@Param("record") BetInit record, @Param("example") BetInitExample example);

    int updateByPrimaryKeySelective(BetInit record);

    int updateByPrimaryKey(BetInit record);

	BetInit getOneBetInitByName(BetInit betInit);

    BetInit selectGameInitMoney(String gName);

}