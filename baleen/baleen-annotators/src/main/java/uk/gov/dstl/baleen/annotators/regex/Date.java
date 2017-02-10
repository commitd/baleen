//Dstl (c) Crown Copyright 2016
package uk.gov.dstl.baleen.annotators.regex;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

import uk.gov.dstl.baleen.annotators.helpers.DateTimeUtils;
import uk.gov.dstl.baleen.types.semantic.Temporal;
import uk.gov.dstl.baleen.uima.BaleenTextAwareAnnotator;
import uk.gov.dstl.baleen.uima.data.TextBlock;

/**
 * Annotate dates and date ranges as Temporal entities. The following examples show the types of dates and ranges that are detected.
 * 
 * <ul>
 * <li>1 December 2016</li>
 * <li>December 1 2016</li>
 * <li>2016-12-01</li>
 * <li>1/12/2016</li>
 * <li>2011-14</li>
 * <li>2011-2016</li>
 * <li>March 2015</li>
 * <li>late August 2016</li> 
 * <li>June-September 2015</li>
 * <li>June 2015 - September 2016</li>
 * <li>10-15 Jan 2015</li>
 * <li>10/11 Jan 2015</li>
 * <li>27th September - 4th October 2016</li>
 * <li>23 December 2016 - 2nd January 2017</li>
 * </ul>
 * 
 * The word 'to' is supported in place of a hyphen, as is the word 'and' if the expression is preceded by 'between'.
 * 
 * Years on their own will only extracted for the range 1970-2099 to reduce false positives. Two digit years on their own will not be extracted.
 * 
 * @baleen.javadoc
 */
public class Date extends BaleenTextAwareAnnotator{
	/**
	 * Should we use American dates where applicable (i.e. mm-dd-yy) 
	 * 
	 * @baleen.config false
	 */
	public static final String PARAM_AMERICAN_FORMAT = "americanDates";
	@ConfigurationParameter(name = PARAM_AMERICAN_FORMAT, defaultValue="false")
	private boolean americanDates;
	
	private static final String DAYS = "(?:(?:Mon|Monday|Tue|Tues|Tuesday|Wed|Wednesday|Thu|Thurs|Thursday|Fri|Friday|Sat|Saturday|Sun|Sunday)\\s+)?";	//Non-capturing as we don't use this information
	private static final String MONTHS = "(Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|Jun(e)?|Jul(y)?|Aug(ust)?|Sep(t(ember)?)?|Oct(ober)?|Nov(ember)?|Dec(ember)?)";
	private static final String DATES = "([0-2]?[0-9]|3[01])\\s*";
	private static final String DATE_SUFFIXES = "(st|nd|rd|th)";
	
	private static final String EXACT = "EXACT";
	private static final String RANGE = "RANGE";
	private static final String SINGLE = "SINGLE";
	private static final String DATE_TYPE = "DATE";
	
	private static final String INVALID_DATE_FOUND = "Invalid date found";
	
	private List<Temporal> extracted;
	
	@Override
	protected void doProcessTextBlock(final TextBlock block) throws AnalysisEngineProcessException {
		extracted = new ArrayList<>();
		
		identifyYearRanges(block);
		identifyMonthYearRanges(block);
		identifyDayMonthYearRanges(block);
		identifyDates(block);
		identifyMonths(block);
		identifyYears(block);
	}
	
	private void identifyYearRanges(final TextBlock block){
		final Pattern longYearShortYear = Pattern.compile("\\b(\\d{2})(\\d{2})-(\\d{2})\\b", Pattern.CASE_INSENSITIVE);
		final String text = block.getCoveredText();
		Matcher m = longYearShortYear.matcher(text);
		
		while(m.find()){
			if(dateSeparatorSuffix(text, m.end())){
				continue;
			}
			
			final Year y1 = Year.parse(m.group(1)+m.group(2));
			final Year y2 = Year.parse(m.group(1)+m.group(3));
			
			createYearTimeRange(block, m.start(), m.end(), y1, y2);
		}
		
		final Pattern longYearLongYear = Pattern.compile("\\b(\\d{4})\\s*(-|to|and)\\s*(\\d{4})\\b", Pattern.CASE_INSENSITIVE);
		m = longYearLongYear.matcher(text);
		
		while(m.find()){
			if("and".equalsIgnoreCase(m.group(2)) && !betweenPrefix(text, m.start())){
				continue;
			}
			
			final Year y1 = Year.parse(m.group(1));
			final Year y2 = Year.parse(m.group(3));
			
			createYearTimeRange(block, m.start(), m.end(), y1, y2);
		}
	}
	
	private void createYearTimeRange(final TextBlock block, final Integer charBegin, final Integer charEnd, final Year y1, final Year y2){
		final Temporal dtg = block.newAnnotation(Temporal.class, charBegin, charEnd);
		dtg.setConfidence(1.0);
		
		dtg.setPrecision(EXACT);
		dtg.setScope(RANGE);
		dtg.setTemporalType(DATE_TYPE);
		
		final LocalDate start = y1.atDay(1);
		final LocalDate end = y2.plusYears(1).atDay(1);
		
		dtg.setTimestampStart(start.atStartOfDay(ZoneOffset.UTC).toEpochSecond());
		dtg.setTimestampStop(end.atStartOfDay(ZoneOffset.UTC).toEpochSecond());
		
		addToJCasIndex(dtg);
		extracted.add(dtg);
	}
	
	private void identifyMonthYearRanges(final TextBlock block){
		final Pattern sameYear = Pattern.compile("\\b"+MONTHS+"\\s*(-|to|and)\\s*"+MONTHS+"\\s+(\\d{4}|'?\\d{2})\\b", Pattern.CASE_INSENSITIVE);
	    final String text = block.getCoveredText();
		Matcher m = sameYear.matcher(text);
		
		while(m.find()){
			if("and".equalsIgnoreCase(m.group(14)) && !betweenPrefix(text, m.start())){
				continue;
			}
			
			final Year y = DateTimeUtils.asYear(m.group(28));
			
			final YearMonth ym1 = y.atMonth(DateTimeUtils.asMonth(m.group(1)));
			final YearMonth ym2 = y.atMonth(DateTimeUtils.asMonth(m.group(15)));
			
			createMonthYearTimeRange(block, m.start(), m.end(), ym1, ym2);
		}
		
		final Pattern diffYear = Pattern.compile("\\b"+MONTHS+"\\s+(\\d{4}|'?\\d{2})\\s*(-|to|and)\\s*"+MONTHS+"\\s+(\\d{4}|'?\\d{2})\\b", Pattern.CASE_INSENSITIVE);
		m = diffYear.matcher(text);
		
		while(m.find()){
			if("and".equalsIgnoreCase(m.group(15)) && !betweenPrefix(text, m.start())){
				continue;
			}
			
			final Year y1 = DateTimeUtils.asYear(m.group(14));
			final YearMonth ym1 = y1.atMonth(DateTimeUtils.asMonth(m.group(1)));
			
			final Year y2 = DateTimeUtils.asYear(m.group(29));
			final YearMonth ym2 = y2.atMonth(DateTimeUtils.asMonth(m.group(16)));
			
			createMonthYearTimeRange(block, m.start(), m.end(), ym1, ym2);
		}
	}
	
	private void createMonthYearTimeRange(final TextBlock block, final Integer charBegin, final Integer charEnd, final YearMonth ym1, final YearMonth ym2){
		final Temporal dtg = block.newAnnotation(Temporal.class, charBegin, charEnd);
		dtg.setConfidence(1.0);
		
		dtg.setPrecision(EXACT);
		dtg.setScope(RANGE);
		dtg.setTemporalType(DATE_TYPE);
		
		final LocalDate start = ym1.atDay(1);
		final LocalDate end = ym2.plusMonths(1).atDay(1);
		
		dtg.setTimestampStart(start.atStartOfDay(ZoneOffset.UTC).toEpochSecond());
		dtg.setTimestampStop(end.atStartOfDay(ZoneOffset.UTC).toEpochSecond());
		
		addToJCasIndex(dtg);
		extracted.add(dtg);
	}
	
	private void identifyDayMonthYearRanges(final TextBlock block){
		final Pattern sameMonth = Pattern.compile("\\b"+DAYS+"([0-2]?[0-9]|3[01])\\s*"+DATE_SUFFIXES+"?\\s*(-|to|and|\\\\|/)\\s*"+DAYS+"([0-2]?[0-9]|3[01])\\s*"+DATE_SUFFIXES+"?\\s+"+MONTHS+"\\s+(\\d{4}|'?\\d{2})\\b", Pattern.CASE_INSENSITIVE);
		final String text = block.getCoveredText();
		Matcher m = sameMonth.matcher(text);
		
		while(m.find()){
			if(!DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(1)), m.group(2)) || !DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(4)), m.group(5))){
				continue;
			}
			
			final Year y = DateTimeUtils.asYear(m.group(19));
			final YearMonth ym = y.atMonth(DateTimeUtils.asMonth(m.group(6)));
			
			LocalDate ld1;
			LocalDate ld2;
			try{
				ld1 = ym.atDay(Integer.parseInt(m.group(1)));
				ld2 = ym.atDay(Integer.parseInt(m.group(4)));
			}catch(final DateTimeException dte){
				getMonitor().warn("Invalid date found", dte);
				continue;
			}
			
			if(("and".equalsIgnoreCase(m.group(3)) && !betweenPrefix(text, m.start())) || "/".equals(m.group(3)) || "\\".equals(m.group(3))){
				if(ld2.equals(ld1.plusDays(1))){
					//Create time range
					createDayMonthYearRange(block, m.start(), m.end(), ld1, ld2);
				}else{
					//Create separate dates as they're not adjacent
					createDate(block, m.start(4), m.end(), ld2);
					
					final Temporal t = createDate(block, m.start(), m.end(), ld1);
					if(t != null)
						t.setValue(text.substring(m.start(), m.start(3)).trim() + " " + text.substring(m.start(6), m.end()).trim());
				}
			}else{
				//Create time range
				createDayMonthYearRange(block, m.start(), m.end(), ld1, ld2);
			}			
		}
		
		Pattern sameYear = Pattern.compile("\\b"+DAYS+DATES+DATE_SUFFIXES+"?\\s+"+MONTHS+"\\s*(-|to|and)\\s*"+DAYS+DATES+DATE_SUFFIXES+"?\\s+"+MONTHS+"\\s+(\\d{4}|'?\\d{2})\\b", Pattern.CASE_INSENSITIVE);
		m = sameYear.matcher(text);
		
		while(m.find()){
			if(!DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(1)), m.group(2)) || !DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(17)), m.group(18))){
				continue;
			}
			if("and".equalsIgnoreCase(m.group(16)) && !betweenPrefix(text, m.start())){
				continue;
			}
			
			final Year y = DateTimeUtils.asYear(m.group(32));
			final YearMonth ym1 = y.atMonth(DateTimeUtils.asMonth(m.group(3)));
			final YearMonth ym2 = y.atMonth(DateTimeUtils.asMonth(m.group(19)));
			
			LocalDate ld1;
			LocalDate ld2;
			try{
				ld1 = ym1.atDay(Integer.parseInt(m.group(1)));
				ld2 = ym2.atDay(Integer.parseInt(m.group(17)));
			}catch(DateTimeException dte){
				getMonitor().warn(INVALID_DATE_FOUND, dte);
				continue;
			}
			
			createDayMonthYearRange(block, m.start(), m.end(), ld1, ld2);
		}
		
		final Pattern fullDates = Pattern.compile("\\b"+DAYS+"([0-2]?[0-9]|3[01])\\s*"+DATE_SUFFIXES+"?\\s+"+MONTHS+"\\s+(\\d{4}|'?\\d{2})\\s*(-|to|and)\\s*"+DAYS+"([0-2]?[0-9]|3[01])\\s*"+DATE_SUFFIXES+"?\\s+"+MONTHS+"\\s+(\\d{4}|'?\\d{2})\\b", Pattern.CASE_INSENSITIVE);
		m = fullDates.matcher(text);
		
		while(m.find()){
			if(!DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(1)), m.group(2)) || !DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(18)), m.group(19))){
				continue;
			}
			if("and".equalsIgnoreCase(m.group(17)) && !betweenPrefix(text, m.start())){
				continue;
			}
			
			final Year y1 = DateTimeUtils.asYear(m.group(16));
			final YearMonth ym1 = y1.atMonth(DateTimeUtils.asMonth(m.group(3)));

			final Year y2 = DateTimeUtils.asYear(m.group(33));
			final YearMonth ym2 = y2.atMonth(DateTimeUtils.asMonth(m.group(20)));			
			
			LocalDate ld1;
			LocalDate ld2;
			try{
				ld1 = ym1.atDay(Integer.parseInt(m.group(1)));
				ld2 = ym2.atDay(Integer.parseInt(m.group(18)));
			}catch(final DateTimeException dte){
				getMonitor().warn(INVALID_DATE_FOUND, dte);
				continue;
			}
			
			createDayMonthYearRange(block, m.start(), m.end(), ld1, ld2);
		}
	}
	
	private void createDayMonthYearRange(final TextBlock block, final Integer charBegin, final Integer charEnd, final LocalDate ld1, final LocalDate ld2){
		final Temporal dtg = block.newAnnotation(Temporal.class, charBegin, charEnd);
		
		dtg.setConfidence(1.0);
		
		dtg.setPrecision(EXACT);
		dtg.setScope(RANGE);
		dtg.setTemporalType(DATE_TYPE);
		
		dtg.setTimestampStart(ld1.atStartOfDay(ZoneOffset.UTC).toEpochSecond());
		dtg.setTimestampStop(ld2.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond());
		
		addToJCasIndex(dtg);
		extracted.add(dtg);
	}
	
	private void identifyDates(final TextBlock block){
		Pattern fullDateDayMonth = Pattern.compile("\\b"+DAYS+DATES+DATE_SUFFIXES+"?\\s+"+MONTHS+",?\\s+(\\d{4}|'?\\d{2}\\b)", Pattern.CASE_INSENSITIVE);
		final String text = block.getCoveredText();
		Matcher m = fullDateDayMonth.matcher(text);
		
		while(m.find()){
			createDateFromMatcher(block, m, 16, 3, 1);
		}
		
		Pattern fullDateMonthDay = Pattern.compile("\\b"+MONTHS+"\\s+([0-2]?[0-9]|3[01])\\s*"+DATE_SUFFIXES+"?,?\\s+(\\d{4}|'?\\d{2}\\b)", Pattern.CASE_INSENSITIVE);
		m = fullDateMonthDay.matcher(text);
		
		while(m.find()){
			createDateFromMatcher(block, m, 16, 1, 14);
		}
		
		Pattern shortDateYearFirst = Pattern.compile("\\b(\\d{4})[-\\\\/\\.](0?[1-9]|1[0-2])[-\\\\/\\.]([0-2]?[0-9]|3[01])\\b", Pattern.CASE_INSENSITIVE);
		m = shortDateYearFirst.matcher(text);
		
		while(m.find()){
			createDateFromMatcher(block, m, 1, 2, 3);
		}
		
		Pattern shortDate = Pattern.compile("\\b([0-2]?[0-9]|3[01])[-\\\\/\\.]([0-2]?[0-9]|3[01])[-\\\\/\\.](\\d{4}|\\d{2})\\b", Pattern.CASE_INSENSITIVE);
		m = shortDate.matcher(text);
		
		while(m.find()){
			final Year y = DateTimeUtils.asYear(m.group(3));
			
			final Integer n1 = Integer.parseInt(m.group(1));
			final Integer n2 = Integer.parseInt(m.group(2));
			
			Integer day;
			Integer month;
			if(n1 >= 1 && n1 <= 12){
				//n1 could be a month or a day
				if(n2 >= 12 && n2 <= 31){
					//n2 must be a day
					month = n1;
					day = n2;
				}else if(n2 >= 1 && n2 <= 12){
					if(americanDates){
						day = n2;
						month = n1;
					}else{
						day = n1;
						month = n2;
					}
				}else{
					//invalid combination of n1 and n2
					continue;
				}
			}else if(n1 >= 1 && n1 <= 31){
				//n1 must be a day
				day = n1;
				if(n2 >= 1 && n2 <= 12){
					//n2 must be a month
					month = n2;
				}else{
					//invalid combination of n1 and n2
					continue;
				}
			}else{
				//n1 can't be a month or a day
				continue;
			}
			
			final YearMonth ym = y.atMonth(month);		
			
			LocalDate ld;
			try{
				ld = ym.atDay(day);
			}catch(final DateTimeException dte){
				getMonitor().warn(INVALID_DATE_FOUND, dte);
				continue;
			}
			
			createDate(block, m.start(), m.end(), ld);
		}
	}
	
	private Temporal createDate(final TextBlock block, final Integer charBegin, final Integer charEnd, final LocalDate ld){
		//Check the date isn't already covered by a range
		if(alreadyExtracted(block, charBegin, charEnd)) {
		  return null;
		}
		
		final Temporal date = block.newAnnotation(Temporal.class, charBegin, charEnd);
		date.setConfidence(1.0);
		
		date.setPrecision(EXACT);
		date.setScope(SINGLE);
		date.setTemporalType(DATE_TYPE);
		
		date.setTimestampStart(ld.atStartOfDay(ZoneOffset.UTC).toEpochSecond());
		date.setTimestampStop(ld.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond());

		addToJCasIndex(date);
		extracted.add(date);
		
		return date;
	}
	
	private boolean alreadyExtracted(final TextBlock block, final Integer blockBegin, final Integer blockEnd) {
	  final int docBegin = block.toDocumentOffset(blockBegin);
	  final int docEnd = block.toDocumentOffset(blockEnd);

      for(final Temporal t : extracted){
        if(t.getBegin() <= docBegin && t.getEnd() >= docEnd){
            return true;
        }
      }    
      return false;
  }

  private void identifyMonths(final TextBlock block){
		final Pattern monthYear = Pattern.compile("\\b((beginning of|start of|early|mid|late|end of)[- ])?"+MONTHS+"\\s+(\\d{4}|'?\\d{2}\\b)", Pattern.CASE_INSENSITIVE);
		final String text = block.getCoveredText();
		final Matcher m = monthYear.matcher(text);
		
		while(m.find()){
			final Year y = DateTimeUtils.asYear(m.group(16));
			final YearMonth ym = y.atMonth(DateTimeUtils.asMonth(m.group(3)));		
			
			if(m.group(2) != null){
				LocalDate ld1;
				LocalDate ld2;
				switch(m.group(2)){
				case "beginning of":
				case "start of":
					ld1 = ym.atDay(1);
					ld2 = ym.atDay(5);
					break;
				case "early":
					ld1 = ym.atDay(1);
					ld2 = ym.atDay(10);
					break;
				case "mid":
					ld1 = ym.atDay(11);
					ld2 = ym.atDay(20);
					break;
				case "late":
					ld1 = ym.atDay(21);
					ld2 = ym.atEndOfMonth();
					break;
				case "end of":
					ld1 = ym.atEndOfMonth().minusDays(5);
					ld2 = ym.atEndOfMonth();
					break;
				default:
					continue;
				}
				
				createDayMonthYearRange(block, m.start(), m.end(), ld1, ld2);
			}else{
				createMonth(block, m.start(), m.end(), ym);
			}
		}
	}
	
	private void createMonth(final TextBlock block, final Integer charBegin, final Integer charEnd, final YearMonth ym){
		//Check the date isn't already covered by a range
      if(alreadyExtracted(block, charBegin, charEnd)) {
        return;
      }
		
		final Temporal date = block.newAnnotation(Temporal.class, charBegin, charEnd);
		
		date.setConfidence(1.0);
		date.setPrecision(EXACT);
		date.setScope(SINGLE);
		date.setTemporalType(DATE_TYPE);
		
		final LocalDate start = ym.atDay(1);
		final LocalDate end = ym.atEndOfMonth();
		
		date.setTimestampStart(start.atStartOfDay(ZoneOffset.UTC).toEpochSecond());
		date.setTimestampStop(end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond());

		addToJCasIndex(date);
		extracted.add(date);
	}
	
	private void identifyYears(final TextBlock block){
		final Pattern monthYear = Pattern.compile("\\b(19[789][0-9]|20[0-9][0-9]\\b)", Pattern.CASE_INSENSITIVE);
		final String text = block.getCoveredText();
		final Matcher m = monthYear.matcher(text);
		
		while(m.find()){
			final Year y = DateTimeUtils.asYear(m.group(1));
			
			createYear(block, m.start(), m.end(), y);
		}
	}
	
	private void createYear(final TextBlock block, final Integer charBegin, final Integer charEnd, final Year y){
		//Check the date isn't already covered by a range
      if(alreadyExtracted(block, charBegin, charEnd)) {
        return;
      }
		
		final Temporal date = block.newAnnotation(Temporal.class, charBegin, charEnd);
		
		date.setConfidence(1.0);
		
		date.setPrecision(EXACT);
		date.setScope(SINGLE);
		date.setTemporalType(DATE_TYPE);

		final LocalDate start = y.atDay(1);
		LocalDate end;
		if(y.isLeap()){
			end = y.atDay(366);
		}else{
			end = y.atDay(365);
		}
		
		date.setTimestampStart(start.atStartOfDay(ZoneOffset.UTC).toEpochSecond());
		date.setTimestampStop(end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond());

		addToJCasIndex(date);
		extracted.add(date);
	}
	
	private static boolean betweenPrefix (final String text, final Integer matchStart){
		return text.substring(0, matchStart)
				.trim().toLowerCase()
				.endsWith("between");
	}
	
	private static boolean dateSeparatorSuffix (final String text, final Integer matchEnd){
		if(matchEnd >= text.length())
			return false;
		
		String nextChar = text.substring(matchEnd, matchEnd + 1);
		return "-".equals(nextChar) || "/".equals(nextChar) || "\\".equals(nextChar);
	}
	
	private void createDateFromMatcher(TextBlock block, Matcher m, Integer yearGroup, Integer monthGroup, Integer dayGroup){
		Year y = DateTimeUtils.asYear(m.group(yearGroup));
		YearMonth ym = y.atMonth(DateTimeUtils.asMonth(m.group(monthGroup)));		
		
		LocalDate ld;
		try{
			ld = ym.atDay(Integer.parseInt(m.group(dayGroup)));
		}catch(DateTimeException dte){
			getMonitor().warn(INVALID_DATE_FOUND, dte);
			return;
		}
		
		createDate(block, m.start(), m.end(), ld);
	}
}
