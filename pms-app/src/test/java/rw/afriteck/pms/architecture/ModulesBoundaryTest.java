package rw.afriteck.pms.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "rw.afriteck.pms")
class ModulesBoundaryTest {
    @ArchTest
    static final ArchRule only_stock_touches_its_internals =
            noClasses().that().resideOutsideOfPackage("rw.afriteck.pms.stock..")
                    .should().dependOnClassesThat().resideInAPackage("rw.afriteck.pms.stock.internal..");

    @ArchTest
    static final ArchRule api_does_not_depend_on_internals =
            noClasses().that().resideInAPackage("rw.afriteck.pms.stock.api..")
                    .should().dependOnClassesThat().resideInAPackage("rw.afriteck.pms.stock.internal..");

    @ArchTest
    static final ArchRule payment_must_not_depend_on_domain_modules =
            noClasses().that().resideInAPackage("rw.afriteck.pms.payment..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "rw.afriteck.pms.restaurant..", "rw.afriteck.pms.pos..", "rw.afriteck.pms.credit..");

    @ArchTest
    static final ArchRule credit_must_not_depend_on_sales_modules =
            noClasses().that().resideInAPackage("rw.afriteck.pms.credit..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "rw.afriteck.pms.restaurant..", "rw.afriteck.pms.pos..");

    @ArchTest
    static final ArchRule nobody_depends_on_platform =
            noClasses().that().resideOutsideOfPackage("rw.afriteck.pms.platform..")
                    .should().dependOnClassesThat().resideInAPackage("rw.afriteck.pms.platform..");
}
