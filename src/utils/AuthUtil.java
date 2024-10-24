package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthUtil {

	private AuthUtil() {}

	/*
	 * 비밀번호 유효성 검사 메소드 
	 * 대소문자 구분 없는 영문자와 숫자, 특수문자가 포함되어 있는지 확인
	 * 비밀번호의 최대길이는 20글자 
	 * 유효하면 true, 유효하지 않으면 false 반환
	 */
	public static boolean isPasswordValid(String passwd) {
		Pattern pattern = null;
		String regex = null;
		Matcher matcher = null;
		
		// 비밀번호의 최대길이가 20글자를 넘어가면 false 반환
		if(passwd.length() > 20) {
			return false;
		}

		// 알파벳이 포함되어 있는지 확인
		regex = "[a-zA-Z]";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(passwd);

		if (!matcher.find()) {
			return false;
		}

		// 숫자가 포함되어 있는지 확인
		regex = "[0-9]";
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(passwd);

		if (!matcher.find()) {
			return false;
		}

		// 특수문자가 포함되어 있는지 확인
		regex = "[^a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\s]"; // 알파벳, 숫자, 한자, 공백을 제외한 문자는 특수문자로 간주
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(passwd);

		if (!matcher.find()) {
			return false;
		}
		
		return true;
	}
}
