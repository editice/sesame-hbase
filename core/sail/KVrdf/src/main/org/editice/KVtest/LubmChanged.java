package org.editice.KVtest;

public class LubmChanged {
	public final static String q1 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT * WHERE "
			+ " {	"
			+ "		?x rdf:type ub:GraduateStudent . "
			+ "		?x ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0> . "
			+ " }";

	public final static String q3 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X "
			+ " WHERE "
			+ " { "
			+ " 		?X rdf:type ub:Publication . "
			+ "		?X ub:publicationAuthor <http://www.Department0.University0.edu/AssistantProfessor0> "
			+ " } ";

	public final static String q4 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X ?Y1 ?Y2 ?Y3 "
			+ " WHERE "
			+ " { "
			+ "		{ ?X rdf:type ub:AssistantProfessor } UNION "
			+ "       { ?X rdf:type ub:AssociateProfessor } UNION "
			+ "       { ?X rdf:type ub:Chair } UNION "
			+ "       { ?X rdf:type ub:Dean } UNION "
			+ "       { ?X rdf:type ub:FullProfessor } UNION "
			+ "       { ?X rdf:type ub:VisitingProfessor } "
			+ "		?X ub:worksFor <http://www.Department0.University0.edu> . "
			+ "		?X ub:name ?Y1 . "
			+ "		?X ub:emailAddress ?Y2 . "
			+ "		?X ub:telephone ?Y3 " + " } ";

	public final static String q5 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X "
			+ " WHERE "
			+ " { "
			+ "		{ ?X rdf:type ub:GraduateStudent } UNION "
			+ "		{ ?X rdf:type ub:UndergraduateStudent } "
			+ "		?X ub:memberOf <http://www.Department0.University0.edu> "
			+ " } ";

	public final static String q6 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X WHERE { { ?X rdf:type ub:GraduateStudent } UNION { ?X rdf:type ub:UndergraduateStudent } } ";

	public final static String q7 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X ?Y "
			+ " WHERE "
			+ " { "
			+ "		{ ?X rdf:type ub:UndergraduateStudent } UNION "
			+ "		{ ?X rdf:type ub:GraduateStudent } "
			+ "		?Y rdf:type ub:Course . "
			+ "		?X ub:takesCourse ?Y . "
			+ "		<http://www.Department0.University0.edu/AssociateProfessor0> ub:teacherOf ?Y "
			+ " } ";

	public final static String q8 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X ?Y ?Z "
			+ " WHERE "
			+ " { "
			+ " 		{ ?X rdf:type ub:UndergraduateStudent } UNION "
			+ " 		{ ?X rdf:type ub:GraduateStudent } "
			+ "		?Y rdf:type ub:Department . "
			+ "		?X ub:memberOf ?Y . "
			+ "		?Y ub:subOrganizationOf <http://www.University0.edu> . "
			+ "		?X ub:emailAddress ?Z " + " } ";

	public final static String q10 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X "
			+ " WHERE "
			+ " { "
			+ " 		?X rdf:type ub:GraduateStudent . "
			+ "		?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0> "
			+ " } ";

	public final static String q11 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X "
			+ " WHERE "
			+ " { "
			+ "		?X rdf:type ub:ResearchGroup . "
			+ "		?X ub:subOrganizationOf ?Y ."
			+ "		?Y ub:subOrganizationOf  <http://www.University0.edu> "
			+ " } ";

	public final static String q12 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X ?Y "
			+ " WHERE "
			+ " { "
			+ " 		?X ub:headOf ?Z. "
			+ "		?Y rdf:type ub:Department . "
			+ "		?X ub:worksFor ?Y . "
			+ "		?Y ub:subOrganizationOf <http://www.University0.edu> " + " } ";

	public final static String q13 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X "
			+ " WHERE "
			+ " { "
			+ "		{ ?X rdf:type ub:Lecturer  . } UNION "
			+ "		{ ?X rdf:type ub:PostDoc  . } UNION "
			+ "		{ ?X rdf:type ub:AssistantProfessor  . } UNION "
			+ "		{ ?X rdf:type ub:AssociateProfessor  . } UNION "
			+ "		{ ?X rdf:type ub:FullProfessor  . } UNION "
			+ "		{ ?X rdf:type ub:VisitingProfessor  . } UNION "
			+ "		{ ?X rdf:type ub:Dean  . } UNION "
			+ "		{ ?X rdf:type ub:TeachingAssistant  . } UNION "
			+ "		{ ?X rdf:type ub:ResearchAssistant  . } UNION "
			+ "		{ ?X rdf:type ub:GraduateStudent  . } UNION "
			+ "		{ ?X rdf:type ub:UndergraduateStudent  . } UNION "
			+ "		{ ?X rdf:type ub:AdministrativeStaff . } "
			+ "		{ ?X ub:undergraduateDegreeFrom <http://www.University0.edu> } UNION "
			+ "		{ ?X ub:mastersDegreeFrom <http://www.University0.edu> } UNION "
			+ "		{ ?X ub:doctoralDegreeFrom <http://www.University0.edu> } "
			+ " } ";

	public final static String q14 = " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ " PREFIX ub: <http://www.nju.edu.cn/#> "
			+ " SELECT ?X "
			+ " WHERE { ?X rdf:type ub:UndergraduateStudent } ";
}
