package rw.afriteck.pms.config;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "rw.afriteck.pms")
class StockBoundaryTest {
    @ArchTest
    static final ArchRule only_stock_touches_its_internals =
            noClasses().that().resideOutsideOfPackage("rw.afriteck.pms.stock..")
                    .should().dependOnClassesThat().resideInAPackage("rw.afriteck.pms.stock.internal..");

    @ArchTest
    static final ArchRule api_does_not_depend_on_internals =
            noClasses().that().resideInAPackage("rw.afriteck.pms.stock.api..")
                    .should().dependOnClassesThat().resideInAPackage("rw.afriteck.pms.stock.internal..");
}
