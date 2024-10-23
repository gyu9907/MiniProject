package recruitment.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.ProjectDBConnection;
import company.domain.CompanyDTO;
import job.domain.JobDTO;
import recruitment.domain.RecruitmentDTO;

public class RecruitmentDAO_imple implements RecruitmentDAO {

	
	// field, attribute, property, 속성
	private Connection conn = ProjectDBConnection.getConn();
	private PreparedStatement pstmt;
	private ResultSet rs;
	
		
	// method, operation, 기능
	
	// === 자원반납을 해주는 메소드 === //
	private void close() { // 조회, 삽입, 수정, 삭제마다 전부 넣으면 너무 길어지고 쓸데없으니 메소드화 해서 한줄로 끝내자!
		try {
			if(rs != null) { rs.close(); rs = null;} // 확인사살
			if(pstmt != null) { pstmt.close(); pstmt = null; }
		} catch (SQLException e) {e.printStackTrace();}
	} // end of private void close()------------
	
	
	// *** 글목록보기를 해주는 메소드 *** //
	@Override
	public List<RecruitmentDTO> recruitmenList() {
		
		List<RecruitmentDTO> recruitmentList = new ArrayList<>();
		
		try {
			String sql = " select recruitment_id, B.name AS comName, J.name AS jobName, title, experience, emp_type, to_char(deadlineday, 'yyyy-mm-dd') AS deadlineday "
					   + " from "
					   + " ( "
					   + " select recruitment_id, fk_company_id, fk_job_id "
					   + "      , case when length(title) > 12 then substr(title, 1, 10) || '..' "
					   + "             else title end AS title "
					   + "      , contents, emp_type, people, salary, registerday, deadlineday, experience, updateday, is_delete "
					   + " from TBL_RECRUITMENT "
					   + " ) A join TBL_COMPANY B "
					   + " on A.fk_company_id = B.company_id "
					   + " join TBL_JOB J "
					   + " on J.job_id = A.fk_job_id order by 1 ";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				
				RecruitmentDTO recruitmentDTO = new RecruitmentDTO();
				recruitmentDTO.setRecruitmentId(rs.getInt("recruitment_id"));
				
				CompanyDTO companyDTO = new CompanyDTO();
				companyDTO.setName(rs.getString("comName"));
				
				recruitmentDTO.setComdto(companyDTO);
				// 채용DTO에 회사DTO 넣기
				
				recruitmentDTO.setTitle(rs.getString("title"));
				recruitmentDTO.setEmpType(rs.getInt("emp_type"));
				recruitmentDTO.setDeadlineday(rs.getString("deadlineday"));
				recruitmentDTO.setExperience(rs.getInt("experience"));
				
				JobDTO jobDTO = new JobDTO();
				jobDTO.setName(rs.getString("jobName"));
				
				recruitmentDTO.setJobdto(jobDTO);
				// 채용DTO에 직종DTO 넣기
				
				recruitmentList.add(recruitmentDTO);
			}
			
			
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		
		return recruitmentList;
	} // end of public List<RecruitmentDTO> recruitmenList()--------------------
	
	
	
	// *** 채용공고 상세보기를 해주는 메소드 *** //
	@Override
	public RecruitmentDTO recruitmentInfoSelect(String recruitmentId) {
		
		RecruitmentDTO recruitmentDTO = null;
		
		try {
			
			String sql = " select recruitment_id, B.name AS comName, title, contents, J.name AS jobName, job_id, experience, emp_type, address, people, salary, to_char(registerday, 'yyyy-mm-dd') AS registerday, to_char(deadlineday, 'yyyy-mm-dd') AS deadlineday "
					   + " from TBL_RECRUITMENT A join TBL_COMPANY B "
					   + " on A.fk_company_id = B.company_id "
					   + " join TBL_JOB J "
					   + " on J.job_id = A.fk_job_id "
					   + " where recruitment_id = ? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, recruitmentId);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) { // 만약 쿼리문 결과가 있다면--
				recruitmentDTO = new RecruitmentDTO();
				recruitmentDTO.setRecruitmentId(rs.getInt("recruitment_id"));	// 채용공고일련번호
				recruitmentDTO.setTitle(rs.getString("title"));					// 채용공고제목
				recruitmentDTO.setContents(rs.getString("contents"));			// 내용
				recruitmentDTO.setExperience(rs.getInt("experience"));			// 경력		   '신입', '경력'
				recruitmentDTO.setEmpType(rs.getInt("emp_type"));				// 채용형태       '정규직', '계약직', '인턴', '프리랜서'
				recruitmentDTO.setPeople(rs.getInt("people"));					// 채용인원
				recruitmentDTO.setSalary(rs.getInt("salary"));					// 연봉
				recruitmentDTO.setRegisterday(rs.getString("registerday"));		// 등록일자
				recruitmentDTO.setDeadlineday(rs.getString("deadlineday"));		// 마감일자
				
				CompanyDTO companyDTO = new CompanyDTO();
				companyDTO.setName(rs.getString("comName")); 	// 회사명
				companyDTO.setAddress(rs.getString("address")); // 주소
				
				recruitmentDTO.setComdto(companyDTO); 			// 회사명, 주소  recruitmentDTO에 넣기
				
				JobDTO jobDTO = new JobDTO();
				jobDTO.setJob_id(rs.getInt("job_id"));			// 직종 일련번호 다른 메소드에서 쓸거임!
				jobDTO.setName(rs.getString("jobName"));		// 직종이름
				
				recruitmentDTO.setJobdto(jobDTO);				// 직종이름 recruitmentDTO에 넣기
				
			} // end of if(rs.next())-------------
			
			
			
		} catch(SQLException e) {
			if(e.getErrorCode()==1722) {
				System.out.println(">> [경고] 글번호는 정수만 가능합니다.!! <<\n");
			}
			else {
				e.printStackTrace();
			}
		} finally {
			close();
		}
		
		
		return recruitmentDTO;
	} // end of public String recruitmentInfoSelect(String recruitmentId)-------------



	// *** 채용공고 등록을 해주는 메소드 *** //
	@Override
	public int recruitmentInsert(CompanyDTO companyDTO, RecruitmentDTO recruitmentDTO) {
		
		int result = 0;
		
		try {
			
			String sql = " insert into TBL_RECRUITMENT(recruitment_id, fk_company_id, fk_job_id, title, contents, emp_type, people, salary, deadlineday, experience)  "
					   + " values(seq_recruitment_id.nextval, 'samsung', ?, ?, ?, ?, ?, ?, to_date(?), ?) ";
			
			pstmt = conn.prepareStatement(sql);
//			pstmt.setInt(1, recruitmentDTO.getFkJobId());
//			pstmt.setString(2, companyDTO.getCompanyId());
//			pstmt.setString(3, recruitmentDTO.getTitle());
//			pstmt.setString(4, recruitmentDTO.getContents());
//			pstmt.setInt(5, recruitmentDTO.getEmpType());
//			pstmt.setInt(6, recruitmentDTO.getPeople());
//			pstmt.setInt(7, recruitmentDTO.getSalary());
//			pstmt.setString(8, recruitmentDTO.getDeadlineday());
//			pstmt.setInt(9, recruitmentDTO.getExperience());
			
			pstmt.setInt(1, recruitmentDTO.getFkJobId());
			pstmt.setString(2, recruitmentDTO.getTitle());
			pstmt.setString(3, recruitmentDTO.getContents());
			pstmt.setInt(4, recruitmentDTO.getEmpType());
			pstmt.setInt(5, recruitmentDTO.getPeople());
			pstmt.setInt(6, recruitmentDTO.getSalary());
			pstmt.setString(7, recruitmentDTO.getDeadlineday());
			pstmt.setInt(8, recruitmentDTO.getExperience());
			
			
			result = pstmt.executeUpdate(); // sql문 실행

			
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		return result;
	}
	
	
		
	// *** 채용공고 수정을 해주는 메소드 *** //
	@Override
	public int recruitmentUpdate(RecruitmentDTO recruitmentDTO) {
		
		int result = 0;
		
		try {
			
			String sql = " update TBL_RECRUITMENT set Title = ?, Contents = ?, fk_job_id = ?, Experience = ?, emp_type = ?, people = ?, salary = ?, deadlineday = to_date(?) "
					   + " where recruitment_id = ? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, recruitmentDTO.getTitle());
			pstmt.setString(2, recruitmentDTO.getContents());
			pstmt.setInt(3, recruitmentDTO.getFkJobId());
			pstmt.setInt(4, recruitmentDTO.getExperience());
			pstmt.setInt(5, recruitmentDTO.getEmpType());
			pstmt.setInt(6, recruitmentDTO.getPeople());
			pstmt.setInt(7, recruitmentDTO.getSalary());
			pstmt.setString(8, recruitmentDTO.getDeadlineday());
			pstmt.setInt(9, recruitmentDTO.getRecruitmentId());
			
			
			result = pstmt.executeUpdate(); // sql문 실행

			
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		return result;
	} // end of public int recruitmentUpdate(RecruitmentDTO recruitmentDTO)----------


	
	// *** 채용공고 삭제를 해주는 메소드 *** //
	@Override
	public int recruitmentDelete(RecruitmentDTO recruitmentDTO) {
		
		int result = 0;
		
		try {
			
			String sql = " delete from TBL_RECRUITMENT where recruitment_id = ? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, recruitmentDTO.getRecruitmentId());
			
			
			result = pstmt.executeUpdate(); // sql문 실행

			
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		return result;
	} // end of public void recruitmentDelete()
	

}