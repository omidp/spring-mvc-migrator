package com.omidp.app;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.openrewrite.java.tree.TypeUtils.isOfClassType;

public class ExplicitWebAnnotationNames extends Recipe {

	@Override
	public String getDisplayName() {
		return "Add explicit spring web annotation names";
	}

	@Override
	public String getDescription() {
		return "Add explicit spring web annotation names to PathVariable and RequestParam.";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			Preconditions.or(
				new UsesType<>("org.springframework.web.bind.annotation.PathVariable", false),
				new UsesType<>("org.springframework.web.bind.annotation.PathVariable", false)
			),
			new ExplicitWebAnnotationNamesVisitor()
		);
	}

	public class ExplicitWebAnnotationNamesVisitor extends JavaIsoVisitor<ExecutionContext> {
		private final Set<String> PARAM_ANNOTATIONS = Stream.of(
			"PathVariable",
			"RequestParam"
		).map(className -> "org.springframework.web.bind.annotation." + className).collect(toSet());

		@Override
		public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext ctx) {
			J.VariableDeclarations varDecls = super.visitVariableDeclarations(multiVariable, ctx);
			if (!varDecls.getLeadingAnnotations().isEmpty()) {
				if (varDecls.getTypeExpression() != null && varDecls.getTypeExpression().getPrefix().getWhitespace().isEmpty()) {
					List<J.Annotation> annotations = varDecls.getLeadingAnnotations();
					J.Annotation lastAnnotation = annotations.get(annotations.size() - 1);
					if (lastAnnotation.getArguments() == null || lastAnnotation.getArguments().isEmpty()) {
						varDecls = varDecls.withTypeExpression(
							varDecls.getTypeExpression().withPrefix(
								varDecls.getTypeExpression().getPrefix().withWhitespace(" ")));
					}
				}
			}
			return varDecls;
		}

		@Override
		public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
			J.Annotation a = super.visitAnnotation(annotation, ctx);
			if (PARAM_ANNOTATIONS.stream().anyMatch(annotationClass -> isOfClassType(annotation.getType(), annotationClass)) &&
				getCursor().getParentOrThrow().getValue() instanceof J.VariableDeclarations) {
				List<Expression> expressionList = a.getArguments();
				if (expressionList == null) {
					expressionList = new ArrayList<>();
				}
				Cursor varDecsCursor = getCursor().getParentOrThrow();
				J.VariableDeclarations.NamedVariable namedVariable = varDecsCursor.<J.VariableDeclarations>getValue().getVariables().get(0);
				for (Expression expression : expressionList) {
					if (expression instanceof J.Literal) {
						return a;
					}
					J.Assignment assignment = (J.Assignment) expression;
					if (assignment.getVariable() instanceof J.Identifier && assignment.getAssignment() instanceof J.Literal) {
						J.Identifier assignName = (J.Identifier) assignment.getVariable();
						if ("value".equals(assignName.getSimpleName()) || "name".equals(assignName.getSimpleName())) {
							return a;
						}
					}
				}

				String annotationValue = "\"" + namedVariable.getSimpleName() + "\"";
				Space space = Space.EMPTY;
				if (expressionList.size() > 0) {
					space = Space.SINGLE_SPACE;
					annotationValue = "value=\"" + namedVariable.getSimpleName() + "\"";
				}
				J.Literal literal = new J.Literal(namedVariable.getId(), space, namedVariable.getMarkers(),
					namedVariable.getSimpleName(), annotationValue, new ArrayList<>(), JavaType.Primitive.String
				);
				expressionList.add(literal);
				return a.withArguments(expressionList);
			}

			return a;
		}
	}

}
