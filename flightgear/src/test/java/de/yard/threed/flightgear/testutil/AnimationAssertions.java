package de.yard.threed.flightgear.testutil;

import de.yard.threed.core.Matrix3;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.flightgear.FlightGearProperties;
import de.yard.threed.flightgear.core.flightgear.main.FGGlobals;
import de.yard.threed.flightgear.core.simgear.scene.model.ACProcessPolicy;
import de.yard.threed.flightgear.core.simgear.scene.model.AnimationGroup;
import de.yard.threed.flightgear.core.simgear.scene.model.SGAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGMaterialAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGRotateAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGScaleAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGSelectAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGTexTransformAnimation;
import de.yard.threed.flightgear.core.simgear.scene.model.SGTranslateAnimation;
import de.yard.threed.flightgear.core.simgear.structure.SGInterpTableExpression;
import de.yard.threed.flightgear.core.simgear.structure.SGPropertyExpression;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static de.yard.threed.core.testutil.TestUtils.assertMatrix3;
import static de.yard.threed.core.testutil.TestUtils.assertVector3;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Also for SGInterpTable(Expression).
 */
@Slf4j
public class AnimationAssertions {

    final static String MCRMCBnodes = "centerBackTranslate->rotateAnimation-GSHBBB->centerTranslate";
    final static String MCRMCBnodesSpin = "centerBackTranslate->spinRotateAnimation-SHBBB->centerTranslate";

    /**
     * See also README.md
     */
    public static void validateWindturbineAnimations(SceneNode xmlNnode, List<SGAnimation> animationList) {
        log.debug(xmlNnode.dump("  ", 0));
        assertEquals(2, animationList.size(), "animations");
        assertNotNull(((SGRotateAnimation) animationList.get(0)).rotategroup, "rotategroup");
        assertNotNull(((SGRotateAnimation) animationList.get(1)).rotategroup, "rotategroup");
        SGRotateAnimation rotateAnimation = (SGRotateAnimation) animationList.get(0);
        SGRotateAnimation spinAnimation = (SGRotateAnimation) animationList.get(1);

        SceneNode tower = xmlNnode.findNodeByName("Tower").get(0);
        SceneNode acWorld = xmlNnode.findNodeByName("ac-world").get(0);
        SceneNode blade1 = xmlNnode.findNodeByName("Blade1").get(0);
        SceneNode generator = xmlNnode.findNodeByName("Generator").get(0);
        // skip intermediate node

        SceneNode firstRotateAnimationGroup = getFirstRotateAnimationGroup(acWorld, 1);
        validateAnimationGroupForRotation(rotateAnimation, "rotateAnimation-GSHBBB",
                new String[]{"Generator", "centerBackTranslate"}, new Vector3(0, 0, 0), new Vector3(0,0,1));
        SceneNode firstSpinRotateGroup = EngineTestUtils.getChild(firstRotateAnimationGroup, 0, 1, 0);
        validateAnimationGroupForRotation(spinAnimation, "spinRotateAnimation-SHBBB",
                new String[]{"Shaft", "Hub", "Blade1", "Blade2", "Blade3"}, new Vector3(0, 0, 81), new Vector3(0,1,0));

        String hierarchy = EngineTestUtils.getHierarchy(blade1, 7, false);
        log.debug("blade1 hierarchy={}", hierarchy);
        assertEquals("ac-world->" + MCRMCBnodes + "->" + MCRMCBnodesSpin + "->Blade1", hierarchy);

        assertEquals("Models/Power/windturbine.xml->ACProcessPolicy.root node->ACProcessPolicy.transform node->Models/Power/windturbine.gltf->gltfroot->ac-world->[Tower,centerBackTranslate]", EngineTestUtils.getHierarchy(xmlNnode, 6, true));

        // be sure the property tree still contains our defaults
        assertEquals(FlightGearProperties.DEFAULT_WIND_FROM_HEADING_DEG, FGGlobals.getInstance().get_props().getNode("/environment/wind-from-heading-deg", true).getDoubleValue());
        assertEquals(FlightGearProperties.DEFAULT_WIND_SPEED_KT, FGGlobals.getInstance().get_props().getNode("/environment/wind-speed-kt", true).getDoubleValue());

        SGRotateAnimation windHeadingRotateAnimation = (SGRotateAnimation) animationList.get(0);
        // not sure calc is correct. 8.12.24 Apparently it is: value 290, factor -1, offset-deg(bias) is -90,
        assertEquals(-1 * FlightGearProperties.DEFAULT_WIND_FROM_HEADING_DEG + -90, windHeadingRotateAnimation.getAnimationValue().doubleVal);
        assertFalse(windHeadingRotateAnimation.isSpin());
        assertVector3((new Vector3(0, 0, 1)), windHeadingRotateAnimation.getAxis());

        SGRotateAnimation windSpeedSpinAnimation = (SGRotateAnimation) animationList.get(1);
        // animation has a random between 0.4 and 0.6, so the effective value might vary
        double value = windSpeedSpinAnimation.getAnimationValue().doubleVal;
        assertTrue(value >= 0.4 * FlightGearProperties.DEFAULT_WIND_SPEED_KT && value <= 0.6 * FlightGearProperties.DEFAULT_WIND_SPEED_KT, "" + value);
        assertTrue(windSpeedSpinAnimation.isSpin());
        assertVector3((new Vector3(0, 1, 0)), windSpeedSpinAnimation.getAxis());

        // change properties and recheck animations
        assertFgWindHeadingRotateAnimation(windHeadingRotateAnimation);

    }

    private static void assertFgWindHeadingRotateAnimation(SGRotateAnimation windHeadingRotateAnimation) {
        // make "/environment/wind-from-heading-deg" the FG basic wheather default 270
        FGGlobals.getInstance().get_props().getNode("/environment/wind-from-heading-deg", true).setDoubleValue(270.0);
        // -360.002 is from FG (-1 * 270 + (-90))
        assertEquals(-360.002, windHeadingRotateAnimation.getAnimationValue().doubleVal, 0.1);
    }

    /**
     * egkk_tower has many animations. Cuurently only 2 are loaded(?) probably due to missing textures and submodel.
     */
    public static void assertEgkkTowerAnimations(SceneNode xmlNnode, List<SGAnimation> animationList, boolean atNight) {
        log.debug(xmlNnode.dump("  ", 1));
        // rotate+textransform+material+rotate+select+translate(2).
        assertEquals(9/*7*/, animationList.size(), "animations");

        SGRotateAnimation elapsedSecRotateAnimation = (SGRotateAnimation) animationList.get(0);
        assertFalse(elapsedSecRotateAnimation.isSpin());
        assertVector3((new Vector3(0, 0, 1)), elapsedSecRotateAnimation.getAxis());
        assertVector3((new Vector3(0, 0, 38.5)), elapsedSecRotateAnimation.getCenter());
        validateAnimationGroupForRotation(elapsedSecRotateAnimation, "rotateAnimation-r",
                new String[]{"radar"}, new Vector3(0, 0, 38.5), new Vector3(0,0,1));

        SGTexTransformAnimation texTransformAnimation = (SGTexTransformAnimation) animationList.get(1);
        Matrix3 expectedTexMatrix = new Matrix3();
        if (atNight) {
            //-0.5 appears correct. But why negative? Just for causing confusion? Leads for 'u' 0.11 to -0.39 wrapping to 0.61?
            expectedTexMatrix.setTranslation(new Vector2(-0.5, 0.0));
        }
        // no transform at all during day time
        assertMatrix3("", expectedTexMatrix, texTransformAnimation.getTransformMatrix());
    }

    /**
     * 22.11.25: More generic to be used in several tests. But there are different asi.xml out there:
     * The 2018 (or earlier) c172p asi.xml has NO center. Later have one! But with same interpolations.
     * FGDATA asi.xml never has a center but different interpolations.
     * And one has material animation for FNC while the other only FN
     */
    public static void assertAsiAnimations(SceneNode someTopLevelNode, List<SGAnimation> animationList,
                                           double currentSpeed, Vector3 expectedCenter) {
        log.debug(someTopLevelNode.dump("  ", 0));

        List<SGAnimation> needleRotateAnimation = FgTestUtils.findAnimationsByObjectNameAndLabelAndId(animationList, "Needle", null, "asi.xml.1");
        // one Material and one RotateAnimation (ASI needle)
        assertEquals(1, needleRotateAnimation.size());

        //SGMaterialAnimation xx = (SGMaterialAnimation) needleAnimations.get(0);
        SGRotateAnimation asiRotateAnimation = (SGRotateAnimation) needleRotateAnimation.get(0);
        SGInterpTableExpression expression = (SGInterpTableExpression) asiRotateAnimation.getAnimationValueExpression();
        //assertTrue(expression instanceof SGInterpTableExpression);
        SGPropertyExpression propertyExpression = (SGPropertyExpression) expression.getOperand();
        // 17.1.25 locomotive-root removed since vehicle no longer have their own tree
        assertEquals("/fdm/jsbsim/velocities/vias-kts", propertyExpression.getPropertyNode().getPath(true));
        SceneNode needle = someTopLevelNode.findNodeByName("Needle").get(0);
        assertNotNull(needle);
        //SceneNode needleRotationGroup = xmlNnode.findNodeByName("Needle").get(0);
        // needle uses interpolation, but no idea whether and how it works. So for now just assume 13.875 is correct.
        // 16.12.24 with implemented interpolation 10.703571 appears also correct


        assertRotateAnimationValues(asiRotateAnimation, needle, currentSpeed == 0.0 ? 0.0 : 10.703571, expectedCenter);

        String hierarchy = EngineTestUtils.getHierarchy(needle, 4, false);
        log.debug("needle hierarchy={}", hierarchy);

        //assertEquals("MaterialAnimationGroup-FN->centerBackTranslate->rotateAnimation-N->centerTranslate->Needle", hierarchy);

    }

    /**
     * windsock model uses windsock specific properties like '/environment/windsock/wind-speed-12.5kt'. It
     * appears the windsock model is a kind of hack.
     */
    public static void assertWindsockAnimations(SceneNode node, List<SGAnimation> animationList) {
        log.debug(node.dump("  ", 0));
        //
        assertEquals(8, animationList.size(), "animations");
        SceneNode acWorld = node.findNodeByName("ac-world").get(0);
        assertNotNull(acWorld);

        SceneNode node2dot5kt = acWorld.findNodeByName("2.5kt").get(0);
        assertNotNull(node2dot5kt);
        String hierarchy = EngineTestUtils.getHierarchy(node2dot5kt, 5, false);
        log.debug("2.5kt hierarchy={}", hierarchy);
        assertEquals("ac-world->centerBackTranslate->rotateAnimation-wf->centerTranslate->windsock->2.5kt", hierarchy);

        SceneNode node5kt = acWorld.findNodeByName("5kt").get(0);
        assertNotNull(node5kt);
        hierarchy = EngineTestUtils.getHierarchy(node5kt, 10, false);
        log.debug("5kt hierarchy={}", hierarchy);

        SceneNode node15kt = acWorld.findNodeByName("15kt").get(0);
        assertNotNull(node15kt);
        hierarchy = EngineTestUtils.getHierarchy(node15kt, 22, false);
        log.debug("15kt hierarchy={}", hierarchy);
        assertEquals("ac-world->" +
                "centerBackTranslate->rotateAnimation-wf->centerTranslate->" +
                "windsock->" +
                "scaleAnimation->" +
                "translateAnimation->" +
                // compared to FG we have 5 rotations. Maybe a FG logging problem
                "centerBackTranslate->rotateAnimation-57111->centerTranslate->" +
                "centerBackTranslate->rotateAnimation-7111->centerTranslate->" +
                "centerBackTranslate->rotateAnimation-111->centerTranslate->" +
                "centerBackTranslate->rotateAnimation-11->centerTranslate->" +
                "centerBackTranslate->rotateAnimation-1->centerTranslate->" +
                "15kt", hierarchy);

        SGRotateAnimation windHeadingRotateAnimation = (SGRotateAnimation) animationList.get(0);
        SGScaleAnimation scaleAnimation = (SGScaleAnimation) animationList.get(1);
        SGTranslateAnimation translateAnimation = (SGTranslateAnimation) animationList.get(2);
        SGRotateAnimation windSpeedRotateAnimation0 = (SGRotateAnimation) animationList.get(3);
        SGRotateAnimation windSpeedRotateAnimation1 = (SGRotateAnimation) animationList.get(4);
        SGRotateAnimation windSpeedRotateAnimation2 = (SGRotateAnimation) animationList.get(5);
        SGRotateAnimation windSpeedRotateAnimation3 = (SGRotateAnimation) animationList.get(6);
        SGRotateAnimation windSpeedRotateAnimation4 = (SGRotateAnimation) animationList.get(7);
        // FG animations think in FG coordinates. So AC axes need switch.
        validateAnimationGroupForRotation(windSpeedRotateAnimation0, "rotateAnimation-57111",
                new String[]{"5kt", "centerBackTranslate"}, new Vector3(0, 1.17, 6.07),new Vector3(-1,0,0));
        assertVector3((new Vector3(-1, 0, 0)), windSpeedRotateAnimation0.getAxis());
        assertVector3((new Vector3(0, 1.17, 6.07)), windSpeedRotateAnimation0.getCenter());


        SGInterpTableExpression interpTableExpression = (SGInterpTableExpression) translateAnimation.getAnimationValueExpression();
        // first some general tests for SGInterpTable
        assertEquals(0.762, interpTableExpression._interpTable.interpolate(0));
        assertEquals(0.762 - 0.0001, interpTableExpression._interpTable.interpolate(1.00001), 0.0001);
        // 1.1 is 40% inside interval
        assertEquals(0.762 - (0.762 - 0.39) * 0.4, interpTableExpression._interpTable.interpolate(1.1), 0.0001);

        // 26.0  > 15 ==> 0.0
        assertEquals(0.0, interpTableExpression.getValue(null).doubleVal);
        assertVector3((new Vector3(0, 1, 0)), translateAnimation.getAxis());

        validateAnimationGroupForRotation(windSpeedRotateAnimation4, "rotateAnimation-1",
                new String[]{"15kt"}, new Vector3(0, 3.28, 5.87),new Vector3(-1,0,0));
        assertVector3((new Vector3(-1, 0, 0)), windSpeedRotateAnimation4.getAxis());
        assertVector3((new Vector3(0, 3.28, 5.87)), windSpeedRotateAnimation4.getCenter());

        // change properties and recheck animations
        assertFgWindHeadingRotateAnimation(windHeadingRotateAnimation);
    }

    /**
     *
     */
    public static void assertBeaconAnimations(SceneNode node, List<SGAnimation> animationList, boolean atNight) {
        log.debug(node.dump("  ", 0));
        //
        assertEquals(7, animationList.size(), "animations");
        SGSelectAnimation selectAnimation = (SGSelectAnimation) animationList.get(0);

        SceneNode acWorld = node.findNodeByName("ac-world").get(0);
        assertNotNull(acWorld);

        SceneNode whiteFlash1 = acWorld.findNodeByName("WhiteFlash.1").get(0);
        assertNotNull(whiteFlash1);
        String hierarchy = EngineTestUtils.getHierarchy(whiteFlash1, 5, false);
        log.debug("whiteFlash1 hierarchy={}", hierarchy);
        assertEquals("ac-world->SelectAnimation-GGWWGW->centerBackTranslate->rotateAnimation-W->centerTranslate->WhiteFlash.1", hierarchy);
        assertEquals(atNight, selectAnimation.isSelected(), "selectAnimation selected");
    }

    /**
     * Validates an {@link AnimationGroup}. For checking values we have assertRotateAnimationValues.
     *
     * @param rotateAnimation
     * @param expectedName
     * @param expectedGrandChildren The intermediate "centerTranslate" node is skipped.
     */
    public static void validateAnimationGroupForRotation(SGRotateAnimation rotateAnimation, String expectedName, String[] expectedGrandChildren, Vector3 expectedCenter, Vector3 expectedAxis) {

        SceneNode animationNode = rotateAnimation.rotategroup;
        //assertEquals("rotateAnimation-N", rotateAnimationNode.getName());
        assertEquals(expectedName, animationNode.getName());
        assertVector3(new Vector3(), animationNode.getTransform().getPosition());

        assertVector3(expectedCenter, rotateAnimation.getCenter(), 0.00001);
        assertVector3(expectedAxis, rotateAnimation.getAxis(), 0.00001);

        // validate intermediate "centerBackTranslate" node
        SceneNode centerBackTranslate = animationNode.getTransform().getParent().getSceneNode();
        assertEquals(1, centerBackTranslate.getTransform().getChildCount());
        assertEquals("centerBackTranslate", centerBackTranslate.getName());
        assertVector3(ACProcessPolicy.fg2ac(expectedCenter), centerBackTranslate.getTransform().getPosition());

        // validate intermediate "centerTranslate" node
        assertEquals(1, animationNode.getTransform().getChildCount());
        SceneNode centerTranslate = animationNode.getTransform().getChild(0).getSceneNode();
        assertEquals("centerTranslate", centerTranslate.getName());
        assertVector3(ACProcessPolicy.fg2ac(expectedCenter).negate(), centerTranslate.getTransform().getPosition());

        assertEquals(expectedGrandChildren.length, centerTranslate.getTransform().getChildCount());
        for (int i = 0; i < expectedGrandChildren.length; i++) {
            assertEquals(expectedGrandChildren[i], centerTranslate.getTransform().getChild(i).getSceneNode().getName());
            // 11.12. not sure the assumption is correct:translate should be done in centerTranslate nodes
            //??assertVector3(new Vector3(), centerTranslate.getTransform().getChild(i).getSceneNode().getTransform().getPosition());
        }
    }

    /**
     * Additional to validateAnimationGroupForRotation() for checking values
     */
    static void assertRotateAnimationValues(SGRotateAnimation rotateAnimation, SceneNode animatedNode, double expectedValue, Vector3 expectedCenter) {

        assertEquals(expectedValue, rotateAnimation.getAnimationValue().doubleVal, 0.00001);
        //TO DO ?? assertQuternon(expectedValue,needleRotationGroup.getTransform().getRotation());


      /*  String hierarchy = EngineTestUtils.getHierarchy(animatedNode, 3, false);
        log.debug("animatedNode hierarchy={}", hierarchy);
        assertEquals("centerBackTranslate->rotateAnimation-N->centerTranslate->" + animatedNode.getName(), hierarchy);*/

    }

    /**
     * Skips "centerBackTranslate" node after child "childindex".
     */
    private static SceneNode getFirstRotateAnimationGroup(SceneNode node, int childIndex) {
        SceneNode firstRotateAnimationGroup = EngineTestUtils.getChild(node, childIndex, 0);
        return firstRotateAnimationGroup;
    }

    /**
     * Top node in expectedHierarchy should be the "ac" node.
     */
    public static void assertAnimationGroupHierarchy(SceneNode node, String expectedHierarchy) {
        String hierarchy = node.getName();
        while (node.getParent() != null) {
            node = node.getParent();
            hierarchy = node.getName() + "->" + hierarchy;
            if (StringUtils.endsWith(node.getName(), ".ac")) {
                break;
            }
        }
        assertEquals(expectedHierarchy, hierarchy);
    }
}
