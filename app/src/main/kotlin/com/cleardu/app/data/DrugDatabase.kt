package com.cleardu.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Drug information data model for the medication search feature.
 */
data class DrugInfo(
    val name: String,
    val genericName: String = "",
    val category: String = "",
    val manufacturer: String = "",
    val description: String = "",
    val indications: String = "",
    val dosage: String = "",
    val administration: String = "",
    val sideEffects: String = "",
    val warnings: String = "",
    val contraindications: String = "",
    val drugInteractions: String = "",
    val storageInfo: String = "",
    val packageInfo: String = "",
    val isFromNetwork: Boolean = false
) {
    /** Convert to MedicationDose for adding to record. */
    fun toMedicationDose(doseMultiplier: Double = 1.0): MedicationDose {
        val detail = if (dosage.isNotEmpty()) dosage else genericName
        return MedicationDose(name = name, detail = detail, doseMultiplier = doseMultiplier)
    }
}

/**
 * Local drug database containing common dialysis-related medications.
 * Provides detailed drug information similar to package inserts.
 */
object DrugDatabase {

    private val drugs: List<DrugInfo> = listOf(
        DrugInfo(
            name = "缬沙坦",
            genericName = "Valsartan",
            category = "降压药 / ARB类",
            manufacturer = "诺华制药",
            description = "缬沙坦是一种血管紧张素II受体拮抗剂(ARB)，通过选择性阻断AT1受体发挥降压作用。适用于轻中度原发性高血压，尤其适用于透析患者。",
            indications = "治疗原发性高血压；降低心血管事件风险；用于心力衰竭患者（不能耐受ACEI者）。透析患者中常用于控制透析间期高血压。",
            dosage = "常用剂量80mg/日，可根据血压调整至160mg/日。透析当日建议在透析后服用。",
            administration = "口服，每日一次，餐前或餐后均可服用。建议固定时间服药以保证稳定的血药浓度。",
            sideEffects = "常见：头晕、头痛、疲劳、低血压。偶见：高钾血症、肾功能异常、咳嗽、皮疹。透析患者需特别注意监测血钾水平。",
            warnings = "双侧肾动脉狭窄患者禁用。严重肝功能不全者慎用。妊娠期禁用。透析患者应定期监测血钾和肾功能。",
            contraindications = "对本品任何成分过敏者；妊娠中晚期；双侧肾动脉狭窄；与阿利吉仑联用（糖尿病患者）。",
            drugInteractions = "与利尿剂合用可增加低血压风险；与保钾利尿剂或钾补充剂合用可致高钾血症；与NSAIDs合用可能减弱降压效果。",
            storageInfo = "遮光，密封，在30°C以下干燥处保存。",
            packageInfo = "80mg×7片/盒，80mg×28片/盒"
        ),
        DrugInfo(
            name = "碳酸钙",
            genericName = "Calcium Carbonate",
            category = "磷结合剂",
            manufacturer = "多家厂商",
            description = "碳酸钙是常用的磷结合剂，通过与食物中的磷酸盐结合形成不溶性复合物，减少肠道对磷的吸收。同时补充钙质，适用于慢性肾脏病伴高磷血症患者。",
            indications = "慢性肾脏病（CKD）患者的高磷血症；预防和治疗钙缺乏；作为透析患者的磷结合剂使用。",
            dosage = "磷结合剂用法：每次500mg-1000mg，随餐服用，根据血磷水平调整剂量。",
            administration = "必须随餐或餐后立即服用，咀嚼后吞服效果更佳。与食物充分混合才能有效结合食物中的磷。",
            sideEffects = "常见：便秘、腹胀、嗳气。偶见：高钙血症、食欲减退、恶心。长期大剂量使用可能导致血管钙化。",
            warnings = "高钙血症患者禁用。应定期监测血钙和血磷水平。避免与含草酸丰富的食物（菠菜、茶叶等）同时服用。",
            contraindications = "高钙血症；严重高钙尿症；维生素D过量；已知对本品过敏者。",
            drugInteractions = "与四环素类抗生素、喹诺酮类药物合用减少吸收；与噻嗪类利尿剂合用增加高钙血症风险；与铁剂间隔2小时服用。",
            storageInfo = "密封，在干燥处保存。",
            packageInfo = "500mg×100片/瓶"
        ),
        DrugInfo(
            name = "促红细胞生成素",
            genericName = "Erythropoietin / EPO",
            category = "抗贫血药 / ESA",
            manufacturer = "多家厂商",
            description = "促红细胞生成素(EPO)是一种糖蛋白激素，能刺激骨髓红系祖细胞的增殖和分化，促进红细胞生成。透析患者因肾脏EPO生成减少常导致肾性贫血。",
            indications = "治疗慢性肾脏病（CKD）引起的贫血，包括透析和非透析患者；化疗引起的贫血；择期手术患者术前自体输血。",
            dosage = "常用剂量：3000-10000 IU/次，皮下注射或静脉注射，每周1-3次。根据血红蛋白水平个体化调整。",
            administration = "皮下注射（腹壁、大腿或上臂）或静脉注射（透析管路回血端）。注射部位应轮换。",
            sideEffects = "常见：高血压、头痛、注射部位疼痛。严重：血栓形成、纯红细胞再生障碍性贫血(PRCA)、癫痫发作。",
            warnings = "未控制的高血压患者慎用。血红蛋白目标值通常不超过115g/L。铁储备不足时应补充铁剂。使用期间需监测血红蛋白和血压。",
            contraindications = "未控制的高血压；已知对本品或白蛋白过敏者；纯红细胞再生障碍性贫血(PRCA)病史。",
            drugInteractions = "与铁剂联用可增强疗效；与ACEI类药物合用可能减弱EPO效果。",
            storageInfo = "2-8°C冷藏保存，不可冷冻。避光保存。",
            packageInfo = "3000IU/支，10000IU/支（预充式注射器）"
        ),
        DrugInfo(
            name = "硝苯地平",
            genericName = "Nifedipine",
            category = "降压药 / 钙通道阻滞剂",
            manufacturer = "拜耳医药",
            description = "硝苯地平是二氢吡啶类钙通道阻滞剂，通过抑制钙离子进入血管平滑肌细胞和心肌细胞，扩张外周血管和冠状动脉，降低血压。",
            indications = "原发性高血压；稳定型心绞痛（慢性）；变异型心绞痛。",
            dosage = "常用剂量：控释片30mg/日，每日一次。根据血压调整，最大剂量60mg/日。",
            administration = "整片吞服，不可咀嚼或掰开。建议在固定时间服用。",
            sideEffects = "常见：头痛、面部潮红、踝部水肿、头晕、心悸。偶见：牙龈增生、便秘、肝功能异常。",
            warnings = "严重主动脉瓣狭窄患者慎用。不稳定心绞痛患者慎用。突然停药可能导致反跳性高血压。",
            contraindications = "心源性休克；不稳定性心绞痛（短效制剂）；急性心肌梗死；已知对本品过敏者。",
            drugInteractions = "与CYP3A4抑制剂（如酮康唑、伊曲康唑）合用增加血药浓度；与利福平合用降低疗效；与β受体阻滞剂合用增强降压效果。",
            storageInfo = "遮光，密封，不超过25°C保存。",
            packageInfo = "30mg×7片/盒，30mg×28片/盒"
        ),
        DrugInfo(
            name = "呋塞米",
            genericName = "Furosemide",
            category = "利尿剂 / 袢利尿剂",
            manufacturer = "多家厂商",
            description = "呋塞米是强效袢利尿剂，作用于髓袢升支粗段，抑制Na+/K+/2Cl-共转运体，产生强大的利尿作用。适用于水肿性疾病和高血压。",
            indications = "水肿性疾病（心力衰竭、肝硬化、肾脏疾病）；高血压；高钙血症；急性肾功能衰竭的预防。",
            dosage = "常用剂量：20-40mg/日，根据利尿效果调整。最大剂量可达600mg/日。",
            administration = "口服，建议早晨服用以减少夜间排尿。可与食物同服减少胃肠不适。",
            sideEffects = "常见：电解质紊乱（低钾、低钠、低镁）、脱水、低血压。偶见：高尿酸血症、血糖升高、耳鸣、听力减退。",
            warnings = "无尿患者禁用。严重电解质紊乱者禁用。应定期监测电解质和肾功能。",
            contraindications = "无尿症；严重低钾血症；严重低钠血症；肝性脑病；磺胺类药物过敏者。",
            drugInteractions = "与氨基糖苷类抗生素合用增加耳毒性；与糖皮质激素合用增加低钾风险；与NSAIDs合用减弱利尿效果。",
            storageInfo = "遮光，密封，不超过25°C保存。",
            packageInfo = "20mg×100片/瓶"
        ),
        DrugInfo(
            name = "阿托伐他汀",
            genericName = "Atorvastatin",
            category = "降脂药 / 他汀类",
            manufacturer = "辉瑞制药",
            description = "阿托伐他汀是HMG-CoA还原酶抑制剂，通过抑制胆固醇合成的限速酶，降低血浆总胆固醇和LDL-C水平。透析患者常伴有血脂异常。",
            indications = "高胆固醇血症；混合型高脂血症；冠心病的一级和二级预防。",
            dosage = "常用剂量：10-20mg/日，每日一次。根据血脂水平调整，最大剂量80mg/日。",
            administration = "每日一次，可在一天中任何时间服用，不受进食影响。",
            sideEffects = "常见：肌痛、关节痛、腹泻、恶心。严重：横纹肌溶解（罕见）、肝酶升高。",
            warnings = "活动性肝病患者禁用。应定期监测肝功能和肌酸激酶。出现肌肉症状应立即就医。",
            contraindications = "活动性肝病；转氨酶持续升高超过正常上限3倍；妊娠期和哺乳期；已知对本品过敏者。",
            drugInteractions = "与CYP3A4抑制剂合用增加横纹肌溶解风险；与环孢素、吉非贝齐合用增加不良反应；与华法林合用可能增强抗凝效果。",
            storageInfo = "密封，不超过25°C保存。",
            packageInfo = "10mg×7片/盒，20mg×7片/盒"
        ),
        DrugInfo(
            name = "活性维生素D",
            genericName = "Calcitriol / Alfacalcidol",
            category = "钙磷代谢调节剂",
            manufacturer = "多家厂商",
            description = "活性维生素D（骨化三醇/阿法骨化醇）是维生素D3的活性代谢产物，能促进肠道钙磷吸收、调节骨代谢、抑制甲状旁腺激素(PTH)分泌。适用于透析患者的肾性骨病。",
            indications = "慢性肾脏病（CKD）患者的继发性甲状旁腺功能亢进；肾性骨营养不良；骨质疏松；甲状旁腺功能减退。",
            dosage = "常用剂量：0.25-0.5μg/日，根据血钙、血磷和PTH水平调整。",
            administration = "口服，每日一次。建议在固定时间服用。",
            sideEffects = "常见：高钙血症、高磷血症。偶见：食欲不振、恶心、呕吐、便秘、头晕。",
            warnings = "高钙血症患者禁用。应定期监测血钙、血磷和PTH水平。避免与含钙磷结合剂同时大量使用。",
            contraindications = "高钙血症；维生素D中毒；已知对本品过敏者。",
            drugInteractions = "与含钙药物合用增加高钙血症风险；与噻嗪类利尿剂合用增加高钙血症风险；与含镁抗酸剂合用可能致高镁血症。",
            storageInfo = "遮光，密封，在阴凉处保存。",
            packageInfo = "0.25μg×30粒/盒"
        ),
        DrugInfo(
            name = "碳酸司维拉姆",
            genericName = "Sevelamer Carbonate",
            category = "非钙磷结合剂",
            manufacturer = "赛诺菲",
            description = "碳酸司维拉姆是一种非钙、非金属的磷结合剂，在胃肠道内与食物中的磷酸盐结合，减少磷的吸收。不含钙和金属，降低高钙血症和金属蓄积风险。",
            indications = "慢性肾脏病（CKD）透析患者的高磷血症。",
            dosage = "常用剂量：800mg/次，每日3次，随餐服用。根据血磷水平每2-3周调整一次剂量。",
            administration = "必须随餐服用，整片吞服，不可咀嚼或压碎。",
            sideEffects = "常见：恶心、呕吐、腹泻、消化不良、便秘、腹痛。",
            warnings = "低磷血症患者慎用。肠梗阻患者禁用。可能影响脂溶性维生素（A、D、E、K）和叶酸的吸收。",
            contraindications = "低磷血症；肠梗阻；已知对本品过敏者。",
            drugInteractions = "与环丙沙星合用时需间隔服用；可能影响脂溶性维生素吸收，建议补充。",
            storageInfo = "密封，不超过25°C保存。",
            packageInfo = "800mg×180片/瓶"
        ),
        DrugInfo(
            name = "琥珀酸亚铁",
            genericName = "Ferrous Succinate",
            category = "补铁剂",
            manufacturer = "多家厂商",
            description = "琥珀酸亚铁是口服铁剂，用于补充铁元素，促进血红蛋白合成。透析患者常因失血和EPO治疗导致铁缺乏，需要补充铁剂。",
            indications = "缺铁性贫血的预防和治疗；慢性肾脏病和透析患者EPO治疗的辅助用药。",
            dosage = "常用剂量：100-200mg/次，每日1-3次。",
            administration = "餐后服用可减少胃肠道刺激。与维生素C同服可增加铁吸收。避免与茶、咖啡、牛奶同服。",
            sideEffects = "常见：恶心、呕吐、便秘、腹泻、黑便、胃部不适。",
            warnings = "血色病或含铁血黄素沉着症患者禁用。铁过载时应停用。",
            contraindications = "血色病；含铁血黄素沉着症；非缺铁性贫血（如地中海贫血）；已知对本品过敏者。",
            drugInteractions = "与抗酸剂、钙剂、四环素类药物合用减少吸收；与茶、咖啡同服降低吸收率；与维生素C合用增加吸收。",
            storageInfo = "遮光，密封，在干燥处保存。",
            packageInfo = "100mg×30片/盒"
        ),
        DrugInfo(
            name = "盐酸司维拉姆",
            genericName = "Sevelamer Hydrochloride",
            category = "非钙磷结合剂",
            manufacturer = "赛诺菲",
            description = "盐酸司维拉姆是一种非吸收性磷结合剂，在肠道内与食物中的磷酸盐结合，阻断磷的吸收。用于CKD透析患者高磷血症的治疗。",
            indications = "慢性肾脏病（CKD）透析患者的高磷血症。",
            dosage = "常用剂量：800mg-1600mg/次，每日3次，随餐服用。根据血磷水平调整。",
            administration = "必须随餐服用，整片吞服。",
            sideEffects = "常见：恶心、呕吐、腹泻、消化不良、便秘。可能引起代谢性酸中毒（盐酸盐型）。",
            warnings = "低磷血症、肠梗阻患者禁用。应定期监测血清碳酸氢盐水平。",
            contraindications = "低磷血症；肠梗阻；已知对本品过敏者。",
            drugInteractions = "与环丙沙星合用时需间隔服用；可能影响脂溶性维生素吸收。",
            storageInfo = "密封，不超过25°C保存。",
            packageInfo = "800mg×180片/瓶"
        ),
        DrugInfo(
            name = "左卡尼汀",
            genericName = "Levocarnitine / L-Carnitine",
            category = "营养补充剂",
            manufacturer = "多家厂商",
            description = "左卡尼汀（左旋肉碱）是脂肪酸代谢的关键载体，促进长链脂肪酸进入线粒体进行氧化供能。透析患者因透析丢失和合成减少常出现肉碱缺乏。",
            indications = "透析患者继发性肉碱缺乏症；原发性肉碱缺乏症；改善心肌能量代谢。",
            dosage = "常用剂量：透析后静脉注射1g/次，每周2-3次。或在透析液中添加。",
            administration = "透析后缓慢静脉推注或加入生理盐水静滴。",
            sideEffects = "罕见：恶心、呕吐、腹泻、体味异常。",
            warnings = "已知对本品过敏者禁用。",
            contraindications = "已知对左卡尼汀过敏者。",
            drugInteractions = "未见明显药物相互作用。",
            storageInfo = "遮光，密封保存。",
            packageInfo = "1g/5ml/支"
        ),
        DrugInfo(
            name = "蔗糖铁",
            genericName = "Iron Sucrose",
            category = "静脉补铁剂",
            manufacturer = "多家厂商",
            description = "蔗糖铁注射液是静脉补铁剂，用于口服铁剂效果不佳或不能耐受的缺铁性贫血患者。透析患者常在透析时经静脉补充铁剂。",
            indications = "口服铁剂效果不佳的缺铁性贫血；慢性肾脏病透析患者的铁补充。",
            dosage = "常用剂量：100mg/次，透析时静脉注射，每周1-2次，累计剂量根据铁缺乏程度计算。",
            administration = "透析时通过透析管路静脉注射，缓慢推注或静脉滴注。首次使用前应进行过敏试验。",
            sideEffects = "常见：注射部位疼痛、金属味感。严重：过敏反应（罕见但可能致命）、低血压。",
            warnings = "首次使用应进行过敏试验。铁过载患者禁用。有药物过敏史者慎用。",
            contraindications = "非缺铁性贫血；铁过载或铁利用障碍；已知对本品过敏者；严重肝病。",
            drugInteractions = "与口服铁剂合用增加铁过载风险；与ACEI类药物合用可能增加过敏风险。",
            storageInfo = "避光，不超过25°C保存，不可冷冻。",
            packageInfo = "100mg/5ml/支"
        ),
        DrugInfo(
            name = "依那普利",
            genericName = "Enalapril",
            category = "降压药 / ACEI类",
            manufacturer = "默沙东",
            description = "依那普利是血管紧张素转换酶抑制剂(ACEI)，通过抑制AngI转化为AngII，降低外周血管阻力，发挥降压作用。",
            indications = "原发性高血压；心力衰竭；无症状性左心室功能不全。",
            dosage = "常用剂量：5-10mg/日，每日1-2次。根据血压调整，最大剂量40mg/日。",
            administration = "口服，餐前或餐后均可。",
            sideEffects = "常见：干咳、头晕、疲劳。偶见：低血压、高钾血症、肾功能损害、血管性水肿（罕见但严重）。",
            warnings = "双侧肾动脉狭窄患者禁用。妊娠期禁用。应定期监测肾功能和血钾。",
            contraindications = "双侧肾动脉狭窄；妊娠期；血管性水肿病史；与阿利吉仑联用（糖尿病患者）。",
            drugInteractions = "与保钾利尿剂合用增加高钾血症风险；与NSAIDs合用减弱降压效果；与锂剂合用增加锂中毒风险。",
            storageInfo = "密封，不超过25°C保存。",
            packageInfo = "5mg×16片/盒，10mg×16片/盒"
        ),
        DrugInfo(
            name = "美托洛尔",
            genericName = "Metoprolol",
            category = "降压药 / β受体阻滞剂",
            manufacturer = "阿斯利康",
            description = "美托洛尔是选择性β1受体阻滞剂，通过减慢心率、降低心肌收缩力、减少心输出量来降低血压。",
            indications = "高血压；心绞痛；心力衰竭；心律失常；心肌梗死后。",
            dosage = "常用剂量：25-100mg/日，分1-2次服用。高血压起始剂量25-50mg/日。",
            administration = "口服，餐后服用可减少胃肠道不适。",
            sideEffects = "常见：疲劳、头晕、心动过缓、四肢发冷。偶见：睡眠障碍、噩梦、支气管痉挛。",
            warnings = "严重心动过缓、II-III度房室传导阻滞患者禁用。突然停药可能引起反跳性高血压。",
            contraindications = "心源性休克；严重心动过缓（<50次/分）；II-III度房室传导阻滞；失代偿性心力衰竭。",
            drugInteractions = "与维拉帕米合用增加心动过缓和心脏传导阻滞风险；与胰岛素合用可能掩盖低血糖症状。",
            storageInfo = "遮光，密封，不超过25°C保存。",
            packageInfo = "25mg×20片/盒，47.5mg×7片/盒（缓释片）"
        ),
        DrugInfo(
            name = "氯化钾",
            genericName = "Potassium Chloride",
            category = "电解质补充剂",
            manufacturer = "多家厂商",
            description = "氯化钾用于预防和治疗低钾血症。透析患者因透析液钾浓度和饮食因素可能出现低钾或高钾，需在医生指导下使用。",
            indications = "低钾血症的预防和治疗；洋地黄中毒引起的快速性心律失常。",
            dosage = "常用剂量：0.5-1g/次，每日2-3次。具体剂量根据血钾水平确定。",
            administration = "口服，餐后服用减少胃肠道刺激。",
            sideEffects = "常见：恶心、呕吐、腹痛、腹泻。严重：高钾血症（心律失常、心脏停搏）。",
            warnings = "高钾血症患者禁用。肾功能不全者慎用。应定期监测血钾水平。",
            contraindications = "高钾血症；严重肾功能不全伴少尿；未经治疗的阿狄森病；急性脱水。",
            drugInteractions = "与保钾利尿剂合用增加高钾血症风险；与ACEI/ARB合用增加高钾血症风险。",
            storageInfo = "密封，在干燥处保存。",
            packageInfo = "0.5g×100片/瓶"
        ),
        DrugInfo(
            name = "碳酸氢钠",
            genericName = "Sodium Bicarbonate",
            category = "酸碱平衡调节剂",
            manufacturer = "多家厂商",
            description = "碳酸氢钠用于纠正代谢性酸中毒。透析患者常因肾功能不全出现代谢性酸中毒，需要补充碱剂。",
            indications = "代谢性酸中毒；碱化尿液；高钾血症的辅助治疗。",
            dosage = "常用剂量：0.5-1g/次，每日3次。根据血气分析结果调整。",
            administration = "口服，餐后服用。",
            sideEffects = "常见：嗳气、腹胀、胃酸反跳。过量：代谢性碱中毒、低钾血症、水肿。",
            warnings = "代谢性或呼吸性碱中毒患者禁用。低钙血症患者慎用（可能诱发手足抽搐）。",
            contraindications = "代谢性或呼吸性碱中毒；低钙血症；严重肾功能不全。",
            drugInteractions = "与酸性药物合用降低疗效；与利尿剂合用可能致低氯性碱中毒。",
            storageInfo = "密封，在干燥处保存。",
            packageInfo = "0.5g×100片/瓶"
        ),
        DrugInfo(
            name = "重组人促红素",
            genericName = "Recombinant Human Erythropoietin / rHuEPO",
            category = "抗贫血药 / ESA",
            manufacturer = "多家厂商",
            description = "重组人促红细胞生成素(rHuEPO)是通过基因工程技术生产的EPO，与内源性EPO具有相同的氨基酸序列，能有效刺激红细胞生成。",
            indications = "肾性贫血（透析和非透析患者）；化疗相关贫血；外科围手术期自体输血。",
            dosage = "初始剂量：50-100 IU/kg，每周3次。维持剂量根据血红蛋白水平调整。",
            administration = "皮下注射或静脉注射。透析患者可在透析结束时经静脉管路给药。",
            sideEffects = "常见：高血压、头痛。严重：血栓栓塞事件、PRCA、高血压危象。",
            warnings = "未控制的高血压患者慎用。血红蛋白增长过快可能增加血栓风险。应监测血红蛋白、血压和铁状态。",
            contraindications = "未控制的高血压；PRCA病史；已知对本品过敏者。",
            drugInteractions = "与铁剂联用增强疗效；与ACEI类药物合用可能减弱EPO效果。",
            storageInfo = "2-8°C冷藏保存，不可冷冻或振摇。",
            packageInfo = "3000IU/支，4000IU/支，10000IU/支"
        ),
        DrugInfo(
            name = "氨氯地平",
            genericName = "Amlodipine",
            category = "降压药 / 钙通道阻滞剂",
            manufacturer = "辉瑞制药",
            description = "氨氯地平是长效二氢吡啶类钙通道阻滞剂，通过扩张外周血管平滑肌降低血压，半衰期长达35-50小时，可平稳控制24小时血压。",
            indications = "原发性高血压；稳定型心绞痛；变异型心绞痛（Prinzmetal心绞痛）。",
            dosage = "常用剂量：5mg/日，根据血压调整，最大剂量10mg/日。",
            administration = "口服，每日一次，固定时间服用。",
            sideEffects = "常见：踝部水肿、头痛、头晕、面部潮红、心悸。",
            warnings = "严重主动脉瓣狭窄患者慎用。肝功能不全者需减量。",
            contraindications = "严重低血压；心源性休克；已知对二氢吡啶类药物过敏者。",
            drugInteractions = "与CYP3A4抑制剂合用增加血药浓度；与CYP3A4诱导剂合用降低疗效；与其他降压药合用增强降压效果。",
            storageInfo = "遮光，密封，不超过25°C保存。",
            packageInfo = "5mg×7片/盒，5mg×28片/盒"
        )
    )

    /**
     * Search drugs by name (Chinese or generic name).
     * Returns matching results with relevance scoring.
     */
    fun search(query: String): List<DrugInfo> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return drugs.filter {
            it.name.lowercase().contains(q) ||
            it.genericName.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }.sortedBy {
            // Prioritize exact name matches
            if (it.name.lowercase() == q) 0
            else if (it.name.lowercase().startsWith(q)) 1
            else if (it.genericName.lowercase().contains(q)) 2
            else 3
        }
    }

    /**
     * Get all drugs.
     */
    fun all(): List<DrugInfo> = drugs

    /**
     * Get drug by exact name.
     */
    fun getByName(name: String): DrugInfo? = drugs.firstOrNull {
        it.name == name || it.genericName.equals(name, ignoreCase = true)
    }
}

/**
 * Network drug search helper.
 * Attempts to fetch drug information from OpenFDA API when not found locally.
 */
object DrugNetworkSearch {

    /**
     * Search for drug info from OpenFDA.
     * Returns empty list if network fails or no results found.
     */
    suspend fun search(query: String): List<DrugInfo> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = java.net.URLEncoder.encode("\"$query\"", "UTF-8")
            val url = URL("https://api.fda.gov/drug/label.json?search=openfda.brand_name:$encodedQuery+openfda.generic_name:$encodedQuery&limit=3")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode != 200) return@withContext emptyList()

            val responseText = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            parseOpenFdaResponse(responseText, query)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseOpenFdaResponse(json: String, query: String): List<DrugInfo> {
        return try {
            val root = JSONObject(json)
            val results = root.optJSONArray("results") ?: return emptyList()
            (0 until results.length()).map { i ->
                val result = results.getJSONObject(i)
                val openfda = result.optJSONObject("openfda")
                val brandName = openfda?.optJSONArray("brand_name")?.optString(0) ?: query
                val genericName = openfda?.optJSONArray("generic_name")?.optString(0) ?: ""
                val manufacturer = openfda?.optJSONArray("manufacturer_name")?.optString(0) ?: ""

                val indications = result.optJSONArray("indications_and_usage")
                    ?.let { (0 until it.length()).joinToString("\n") { j -> it.optString(j) } } ?: ""
                val dosage = result.optJSONArray("dosage_and_administration")
                    ?.let { (0 until it.length()).joinToString("\n") { j -> it.optString(j) } } ?: ""
                val warnings = result.optJSONArray("warnings")
                    ?.let { (0 until it.length()).joinToString("\n") { j -> it.optString(j) } } ?: ""
                val sideEffects = result.optJSONArray("adverse_reactions")
                    ?.let { (0 until it.length()).joinToString("\n") { j -> it.optString(j) } } ?: ""
                val description = result.optJSONArray("description")
                    ?.let { (0 until it.length()).joinToString("\n") { j -> it.optString(j) } } ?: ""

                DrugInfo(
                    name = brandName,
                    genericName = genericName,
                    category = "处方药",
                    manufacturer = manufacturer,
                    description = description.take(500),
                    indications = indications.take(500),
                    dosage = dosage.take(500),
                    sideEffects = sideEffects.take(500),
                    warnings = warnings.take(500),
                    isFromNetwork = true
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}