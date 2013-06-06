package org.editice.KVtest;

public class LubmQueryStatics {

	// TODO "+" need to be in another line according to the java coding
	// standards
	private LubmQueryStatics() {
		throw new Error("not to Instantiated!");
	}

	public static final String prefix1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
	public static final String prefix2 = "PREFIX ub: <http://www.nju.edu.cn/#>";
	// public static final String
	// prefix2="PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>";

	public static final String prefix = prefix1 + prefix2;
	public static final String q1_a = prefix
			+ "SELECT ?X "
			+ "WHERE"
			+ "{ ?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0> ."
			+ "?X rdf:type ub:GraduateStudent }";
	public static final String q1 = prefix + "SELECT ?X " + "WHERE"
			+ "{?X rdf:type ub:GraduateStudent ." + " ?X ub:takesCourse "
			+ "<http://www.Department0.University0.edu/GraduateCourse0>}";
	public static final String q2 = prefix + "	SELECT ?X ?Y ?Z " + "WHERE "
			+ "{?X rdf:type ub:UndergraduateStudent ."
			+ " ?Y rdf:type ub:University ." + "?Z rdf:type ub:Department ."
			+ "?X ub:memberOf ?Z ." + "?Z ub:subOrganizationOf ?Y ."
			+ "?X ub:undergraduateDegreeFrom ?Y}";
	public static final String q3 = prefix + "SELECT ?X " + "WHERE"
			+ "{?X rdf:type ub:Publication ." + " ?X ub:publicationAuthor "
			+ "<http://www.Department0.University0.edu/AssistantProfessor0>}";
	public static final String q3_a = prefix
			+ "SELECT ?X "
			+ "WHERE"
			+ "{?X ub:publicationAuthor <http://www.Department0.University0.edu/AssistantProfessor0> ."
			+ "?X rdf:type ub:Publication }";
	public static final String q4 = prefix + "SELECT ?X ?Y1 ?Y2 ?Y3" + "WHERE"
			+ "{?X rdf:type ub:Professor ."
			+ "?X ub:worksFor <http://www.Department0.University0.edu> ."
			+ "?X ub:name ?Y1 ." + "?X ub:emailAddress ?Y2 ."
			+ "?X ub:telephone ?Y3}";
	public static final String q5 = prefix + "SELECT ?X" + "WHERE"
			+ "{?X rdf:type ub:Person ."
			+ "?X ub:memberOf <http://www.Department0.University0.edu>}";
	public static final String q6 = prefix
			+ "SELECT ?X WHERE {?X rdf:type ub:Student}";
	public static final String q7 = prefix + "SELECT ?X ?Y" + "WHERE"
			+ "{?X rdf:type ub:Student ." + "?Y rdf:type ub:Course ."
			+ "?X ub:takesCourse ?Y ."
			+ "<http://www.Department0.University0.edu/AssociateProfessor0>  "
			+ "ub:teacherOf ?Y}";
	public static final String q8 = prefix + "SELECT ?X ?Y ?Z" + "WHERE"
			+ "{?X rdf:type ub:Student ." + "?Y rdf:type ub:Department ."
			+ "?X ub:memberOf ?Y ."
			+ "?Y ub:subOrganizationOf <http://www.University0.edu> ."
			+ "?X ub:emailAddress ?Z}";
	public static final String q9 = prefix + "SELECT ?X ?Y ?Z" + "WHERE"
			+ "{?X rdf:type ub:Student ." + "?Y rdf:type ub:Faculty ."
			+ "?Z rdf:type ub:Course ." + "?X ub:advisor ?Y ."
			+ "?Y ub:teacherOf ?Z ." + "?X ub:takesCourse ?Z}";
	public static final String q10 = prefix + "SELECT ?X" + "WHERE"
			+ "{?X rdf:type ub:Student ." + "?X ub:takesCourse"
			+ "<http://www.Department0.University0.edu/GraduateCourse0>}";
	public static final String q11 = prefix + "SELECT ?X" + "WHERE"
			+ "{?X rdf:type ub:ResearchGroup ."
			+ "?X ub:subOrganizationOf <http://www.University0.edu>}";
	public static final String q11_a = prefix + "SELECT ?X" + "WHERE"
			+ "{?X ub:subOrganizationOf <http://www.University0.edu> ."
			+ "?X rdf:type ub:ResearchGroup }";
	public static final String q12 = prefix + "SELECT ?X ?Y" + "WHERE"
			+ "{?X rdf:type ub:Chair ." + "?Y rdf:type ub:Department ."
			+ "?X ub:worksFor ?Y ."
			+ "?Y ub:subOrganizationOf <http://www.University0.edu>}";
	public static final String q13 = prefix + "SELECT ?X" + "WHERE"
			+ "{?X rdf:type ub:Person ."
			+ "<http://www.University0.edu> ub:hasAlumnus ?X}";
	public static final String q14 = prefix + "SELECT ?X"
			+ "WHERE {?X rdf:type ub:UndergraduateStudent}";

	public static final String test = prefix
			+ "SELECT ?X "
			+ "WHERE"
			+ "{<http://www.Department0.University0.edu/GraduateStudent44> rdf:type ?X }";

	public static final String test1 = prefix + "SELECT ?X " + "WHERE"
			+ "{?X rdf:type <http://www.nju.edu.cn/#GraduateStudent> }";

	public static final String test2 = prefix + "SELECT ?X " + "WHERE"
			+ "{ ?X ub:takesCourse "
			+ "<http://www.Department0.University0.edu/GraduateCourse0>}";

	public static final String test3 = prefix + "SELECT ?X ?Y " + "WHERE"
			+ "{?X ub:takesCourse ?Y }";

	public static final String test4 = prefix + "SELECT ?X " + "WHERE"
			+ "{?X ub:type ub:University  }";

	public static final String test5 = prefix + "SELECT ?X ?Y" + "WHERE"
			+ "{?X ub:type ?Y  }";

	public static final String test6 = prefix + "SELECT ?X " + "WHERE"
	+ "{?X rdf:type ub:GraduateStudent ." + " ?X ub:takesCourse "
	+ "<http://www.Department0.University0.edu/GraduateCourse0> ." 
			+"?X ub:memberOf <http://www.Department0.University0.edu> }";

	public static final String test7="SELECT ?X "+"WHERE"+
	"{?X <http://www.nju.edu.cn/#headOf> <http://www.Department10.University39.edu>}";
	
	public static final String test8="SELECT ?X "+"WHERE"+
	"{?X <http://www.nju.edu.cn/#undergraduateDegreeFrom> <http://www.University4.edu>}";
	
	public static final String q2_a = 
		" PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		" PREFIX ub: <http://www.nju.edu.cn/#> " +
		" SELECT ?X ?Y ?Z " +
		" WHERE " +
		" { " +
//		"		?X rdf:type ub:GraduateStudent . " +
		"		?Y rdf:type ub:University . " +
		"		?Z rdf:type ub:Department . " +
		"		?X ub:memberOf ?Z . " +
//		"		?Z ub:subOrganizationOf ?Y . " +
		"		?X ub:undergraduateDegreeFrom ?Y " +
		" }";

}
