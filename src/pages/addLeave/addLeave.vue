<template>
	<view class="m3-page m3-form">
		<view class="m3-appbar">
			<view class="m3-appbar-lead" @click="back">
				<text class="m3-back">‹</text>
			</view>
			<view class="m3-appbar-title"><text>添加请假</text></view>
		</view>

		<view class="m3-section">
			<text class="m3-section-title">请假信息</text>

			<view class="m3-field">
				<text class="m3-label">姓名</text>
				<input class="m3-input" v-model="name" placeholder="请输入姓名" />
			</view>

			<view class="m3-field">
				<text class="m3-label">开始时间</text>
				<view class="m3-row">
					<view class="m3-pick" @click="openPicker('date','startDate','开始日期')">
						<text :class="startDate ? 'm3-pick-val' : 'm3-pick-ph'">{{ startDate || '开始日期' }}</text>
					</view>
					<view class="m3-pick" @click="openPicker('time','startTime','开始时间')">
						<text :class="startTime ? 'm3-pick-val' : 'm3-pick-ph'">{{ startTime || '开始时间' }}</text>
					</view>
				</view>
			</view>

			<view class="m3-field">
				<text class="m3-label">结束时间</text>
				<view class="m3-row">
					<view class="m3-pick" @click="openPicker('date','endDate','结束日期')">
						<text :class="endDate ? 'm3-pick-val' : 'm3-pick-ph'">{{ endDate || '结束日期' }}</text>
					</view>
					<view class="m3-pick" @click="openPicker('time','endTime','结束时间')">
						<text :class="endTime ? 'm3-pick-val' : 'm3-pick-ph'">{{ endTime || '结束时间' }}</text>
					</view>
				</view>
			</view>

			<view class="m3-field">
				<text class="m3-label">请假原因</text>
				<input class="m3-input" v-model="reason" placeholder="不少于 10 个字" />
			</view>

			<view class="m3-field">
				<text class="m3-label">紧急联系人</text>
				<input class="m3-input" v-model="phone" placeholder="手机号" />
			</view>

			<view class="m3-field">
				<text class="m3-label">目的地</text>
				<input class="m3-input" v-model="destination" placeholder="省/市/区/街道" />
			</view>

			<view class="m3-field">
				<text class="m3-label">宿舍</text>
				<view class="m3-prefix-row">
					<text class="m3-prefix">{{ dormPrefix }}</text>
					<input class="m3-input m3-input-inline" v-model="dormSuffix" placeholder="235-1" />
				</view>
			</view>

			<view class="m3-field">
				<text class="m3-label">定位位置</text>
				<input class="m3-input" v-model="address" placeholder="当前位置" />
			</view>
		</view>

		<view class="m3-section">
			<text class="m3-section-title">附件</text>
			<view class="m3-file" @click="clickFile">
				<image v-if="imgFile" class="m3-file-img" :src="imgFile" mode="aspectFill"></image>
				<view v-else class="m3-file-empty">
					<text class="m3-file-plus">＋</text>
					<text class="m3-file-hint">添加图片附件</text>
				</view>
			</view>
		</view>

		<view class="m3-section">
			<text class="m3-section-title">审批信息</text>

			<view class="m3-field">
				<text class="m3-label">一级审批人</text>
				<input class="m3-input" v-model="oneCheck" placeholder="辅导员姓名" />
			</view>

			<view class="m3-field">
				<text class="m3-label">审批通过时间</text>
				<view class="m3-row">
					<view class="m3-pick" @click="openPicker('date','oneCheckDate','审批日期')">
						<text :class="oneCheckDate ? 'm3-pick-val' : 'm3-pick-ph'">{{ oneCheckDate || '审批日期' }}</text>
					</view>
					<view class="m3-pick" @click="openPicker('time','oneCheckTime','审批时间')">
						<text :class="oneCheckTime ? 'm3-pick-val' : 'm3-pick-ph'">{{ oneCheckTime || '审批时间' }}</text>
					</view>
				</view>
			</view>
		</view>

		<view class="m3-submit-wrap">
			<view class="m3-submit" @click="sub">提交申请</view>
		</view>

		<!-- MD3 Snackbar（不拦截操作） -->
		<view class="m3-snackbar" v-if="snack.show">{{ snack.text }}</view>

		<!-- ===== MD3 选择器 ===== -->
		<view class="m3-scrim" v-if="picker.show" @click="closePicker" @touchmove.stop.prevent></view>
		<view class="m3-sheet" v-if="picker.show">
			<view class="m3-sheet-grip"></view>
			<view class="m3-sheet-head">
				<text class="m3-sheet-btn" @click="closePicker">取消</text>
				<text class="m3-sheet-title">{{ picker.title }}</text>
				<text class="m3-sheet-btn is-primary" @click="confirmPicker">确定</text>
			</view>
			<view class="m3-wheel">
				<scroll-view v-for="(col,ci) in picker.cols" :key="ci" class="m3-wheel-col" scroll-y
					:scroll-top="picker.scroll[ci]" :scroll-with-animation="true"
					@scroll="onColScroll(ci, $event)">
					<view class="m3-wheel-pad"></view>
					<view v-for="(it,ii) in col" :key="ii" class="m3-wheel-item"
						:class="{ 'is-on': picker.sel[ci] === ii }" @click="pickIdx(ci, ii)">{{ it }}</view>
					<view class="m3-wheel-pad"></view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				leaves: null,
				snack: { show: false, text: '' },
				picker: {
					show: false,
					mode: 'date',
					title: '',
					target: '',
					cols: [],
					sel: [0, 0, 0],
					scroll: [0, 0, 0]
				},
				name: '',
				startDate: '',
				startTime: '',
				endDate: '',
				endTime: '',
				reason: '',
				phone: '',
				address: '',
				cc: '无',
				destination: '',
				dormPrefix: '北区-N10-N10B',
				dormSuffix: '',
				leaveSchool: '离校',
				oneCheck: '',
				oneCheckDate: '',
				oneCheckTime: '',
				oneCheckStatus: '辅导员',
				oneCheckIdea: '无',
				state: '正在休假中',
				type: '外出请假',
				colorIndex: 0,
				imgFile:'',
			}
		},
		computed: {
			computedCount() {
				const s = this.toDate(this.startDate, this.startTime);
				const e = this.toDate(this.endDate, this.endTime);
				if (!s || !e) {
					return '';
				}
				let mins = Math.round((e - s) / 60000);
				if (!isFinite(mins) || mins <= 0) {
					return '';
				}
				const d = Math.floor(mins / 1440);
				const h = Math.floor((mins % 1440) / 60);
				const m = mins % 60;
				let out = '';
				if (d > 0) {
					out += d + '天';
				}
				if (d > 0 || h > 0) {
					out += h + '小时';
				}
				out += m + '分钟';
				return out;
			}
		},
		onShow() {
			this.leaves = uni.getStorageSync('todaySchoolLeaves');
			if (this.leaves == '' || this.leaves == null) {
				this.leaves = [];
			}
			if(this.leaves.length > 0){
				var leave = this.leaves[this.leaves.length - 1];
				this.name = leave.name;
				this.reason = leave.reason;
				this.phone = leave.phone;
				this.address = leave.address;
				this.destination = leave.destination || '';
				this.dormSuffix = String(leave.dorm || '').replace(this.dormPrefix, '');
				this.oneCheck = leave.oneCheck;
			}
		},
		methods: {
			// ===== MD3 选择器 =====
			openPicker(mode, target, title) {
				var cols = [];
				var sel = [0, 0, 0];
				var now = new Date();
				if (mode === 'date') {
					var years = [];
					for (var y = now.getFullYear() - 1; y <= now.getFullYear() + 2; y++) {
						years.push(y + '年');
					}
					var months = [];
					for (var m = 1; m <= 12; m++) {
						months.push((m < 10 ? '0' : '') + m + '月');
					}
					var days = [];
					for (var d = 1; d <= 31; d++) {
						days.push((d < 10 ? '0' : '') + d + '日');
					}
					cols = [years, months, days];
					var cur = this[target] || '';
					var parts = cur.split('-');
					if (parts.length === 2) {
						sel[0] = Math.max(0, now.getFullYear() - (now.getFullYear() - 1));
						sel[1] = parseInt(parts[0], 10) - 1;
						sel[2] = parseInt(parts[1], 10) - 1;
					} else {
						sel = [1, now.getMonth(), now.getDate() - 1];
					}
				} else {
					var hours = [];
					for (var h = 0; h < 24; h++) {
						hours.push((h < 10 ? '0' : '') + h + '时');
					}
					var mins = [];
					for (var mi = 0; mi < 60; mi++) {
						mins.push((mi < 10 ? '0' : '') + mi + '分');
					}
					cols = [hours, mins];
					var t = (this[target] || '').split(':');
					if (t.length === 2) {
						sel = [parseInt(t[0], 10), parseInt(t[1], 10)];
					} else {
						sel = [now.getHours(), now.getMinutes()];
					}
				}
				var scroll = [];
				for (var i = 0; i < cols.length; i++) {
					scroll.push(sel[i] * 44);
				}
				this.picker = {
					show: true,
					mode: mode,
					title: title,
					target: target,
					cols: cols,
					sel: sel,
					scroll: [0, 0, 0]
				};
				this.lockPage(true);
				var that = this;
				setTimeout(function() {
					that.picker.scroll = scroll;
				}, 80);
			},

			onColScroll(ci, e) {
				var top = (e.detail && e.detail.scrollTop) || 0;
				var idx = Math.round(top / 44);
				var max = this.picker.cols[ci].length - 1;
				if (idx < 0) {
					idx = 0;
				}
				if (idx > max) {
					idx = max;
				}
				if (this.picker.sel[ci] !== idx) {
					var sel = this.picker.sel.slice();
					sel[ci] = idx;
					this.picker.sel = sel;
				}
				var that = this;
				clearTimeout(this._snapTimer);
				this._snapTimer = setTimeout(function() {
					var sc = that.picker.scroll.slice();
					sc[ci] = idx * 44;
					that.picker.scroll = sc;
				}, 160);
			},

			closePicker() {
				this.picker.show = false;
				this.lockPage(false);
			},

			lockPage(lock) {
				// #ifdef H5
				var els = [
					document.documentElement,
					document.body,
					document.querySelector('uni-page-wrapper'),
					document.querySelector('uni-page-body')
				];
				if (lock) {
					this._lockEls = [];
					for (var i = 0; i < els.length; i++) {
						var el = els[i];
						if (!el) {
							continue;
						}
						this._lockEls.push([el, el.style.overflow]);
						el.style.overflow = 'hidden';
					}
				} else {
					var list = this._lockEls || [];
					for (var j = 0; j < list.length; j++) {
						list[j][0].style.overflow = list[j][1];
					}
					this._lockEls = [];
				}
				// #endif
			},

			pickIdx(ci, ii) {
				var sel = this.picker.sel.slice();
				sel[ci] = ii;
				var scroll = this.picker.scroll.slice();
				scroll[ci] = ii * 44;
				this.picker.sel = sel;
				this.picker.scroll = scroll;
			},

			confirmPicker() {
				var pk = this.picker;
				var val = '';
				if (pk.mode === 'date') {
					var month = pk.sel[1] + 1;
					var day = pk.sel[2] + 1;
					var year = new Date().getFullYear() - 1 + pk.sel[0];
					var maxDay = new Date(year, month, 0).getDate();
					if (day > maxDay) {
						day = maxDay;
					}
					val = (month < 10 ? '0' : '') + month + '-' + (day < 10 ? '0' : '') + day;
				} else {
					val = (pk.sel[0] < 10 ? '0' : '') + pk.sel[0] + ':' + (pk.sel[1] < 10 ? '0' : '') + pk.sel[1];
				}
				this[pk.target] = val;
				this.picker.show = false;
				this.lockPage(false);
			},

			back() {
				uni.navigateBack({ delta: 1 });
			},
			// "MM-DD" + "HH:mm" -> Date
			toDate(dateStr, timeStr) {
				if (!dateStr || !timeStr) {
					return null;
				}
				const dp = String(dateStr).split('-');
				const tp = String(timeStr).split(':');
				if (dp.length < 2 || tp.length < 2) {
					return null;
				}
				return new Date(2020, Number(dp[0]) - 1, Number(dp[1]), Number(tp[0]), Number(tp[1]));
			},

			sub: function() {
				var that = this;
				if (this.name == '' || this.startDate == '' || this.startTime == '' ||
					this.endDate == '' || this.endTime == '' || this.reason == '' || this.address == '' ||
					this.phone == '' || this.destination == '' || this.dormSuffix == '' || this.oneCheckTime == '' ||
					this.oneCheck == '' || this.computedCount == '' || this.oneCheckDate == '') {
					this.toast("请将必填信息填写完整", "none");
					return;
				}
				if (this.startDate == this.endDate && this.startTime == this.endTime) {
					this.toast("开始日期不能等于结束日期", "none");
					return;
				}
				if (this.reason.length < 10) {
					this.toast("请假原因不能少于10字", "none");
					return;
				}
				var obj = {};
				var date = new Date();
				obj['name'] = this.name;
				obj['type'] = this.type;
				obj['startDate'] = this.startDate + " " + this.startTime;
				obj['endDate'] = this.endDate + " " + this.endTime;
				obj['countDate'] = this.computedCount;
				obj['reason'] = this.reason;
				obj['phone'] = this.phone;
				obj['address'] = this.address;
				obj['cc'] = this.cc;
				obj['destination'] = this.destination;
				obj['dorm'] = this.dormPrefix + this.dormSuffix;
				obj['leaveSchool'] = this.leaveSchool;
				obj['oneCheck'] = this.oneCheck;
				obj['oneCheckDate'] = this.oneCheckDate + " " + this.oneCheckTime;
				obj['oneCheckStatus'] = '辅导员';
				obj['oneCheckIdea'] = '无';
				obj['state'] = this.state;
				obj['applyDate'] = (Number(date.getMonth() + 1).toString() <= 1 ? "0" + Number(date.getMonth() + 1) : Number(date.getMonth() +
						1)) + "-" +
					(date.getDate().toString().length <= 1 ? "0" + date.getDate() : date.getDate()) + " " +
					(date.getHours().toString().length <= 1 ? "0" + date.getHours() : date.getHours()) + ":" +
					(date.getMinutes().toString().length <= 1 ? "0" + date.getMinutes() : date.getMinutes());
			
				obj['imgFile'] = this.imgFile;
				this.leaves.push(obj);
				console.log(this.leaves)
				uni.setStorage({
					key: 'todaySchoolLeaves',
					data: that.leaves,
					success: function() {
						that.toast("提交成功", "success");
						setTimeout(function() {
							uni.navigateBack({
								delta: 1
							})
						}, 1600)
					},
					fail: function() {
						that.toast("提交失败，请重试", "none");
					}
				})
			},

			//开始日期选择
			bindStartDate: function(e) {
				console.log(e.detail.value.substring(5, e.detail.value.length))
				this.startDate = e.detail.value.substring(5, e.detail.value.length)
			},
			//开始时间选择
			bindStartTime: function(e) {
				console.log(e.detail.value)
				this.startTime = e.detail.value;
			},
			//结束日期选择
			bindEndDate: function(e) {
				console.log(e.detail.value.substring(5, e.detail.value.length))
				this.endDate = e.detail.value.substring(5, e.detail.value.length)
			},
			//结束时间选择
			bindEndTime: function(e) {
				console.log(e.detail)
				this.endTime = e.detail.value;
			},
			//一级审批日期选择
			bindOneCheckDate: function(e) {
				console.log(e.detail.value.substring(5, e.detail.value.length))
				this.oneCheckDate = e.detail.value.substring(5, e.detail.value.length)
			},
			//一级审批时间选择
			bindOneCheckTime: function(e) {
				console.log(e.detail)
				this.oneCheckTime = e.detail.value;
			},
			//实际休假时间颜色选择被单击
			bindColor:function(e){
				console.log('picker发送选择改变，携带值为', e.target.value)
				this.colorIndex = e.target.value;
				this.changColorTitleStyle()
			},
			//实际休假时间颜色标题颜色改变
			changColorTitleStyle:function(){
				if(this.colorIndex == 0){
					return 'line-height: 45px;color:#F79A0D;';
				} else if(this.colorIndex == 1){
					return 'line-height: 45px;color:black;';
				} else if(this.colorIndex == 2){
					return 'line-height: 45px;color:red;';
				}
			},
			// ---- H5：动态创建真正的原生 date/time 选择器 ----
			openNativePicker(type, value, onPick) {
				// #ifdef H5
				var input = document.createElement('input');
				input.type = type;
				input.value = value || '';
				input.style.position = 'fixed';
				input.style.left = '0';
				input.style.top = '0';
				input.style.width = '1px';
				input.style.height = '1px';
				input.style.opacity = '0';
				input.style.border = 'none';
				input.style.padding = '0';
				input.style.zIndex = '99999';
				document.body.appendChild(input);
				var closed = false;
				var close = function() {
					if (closed) {
						return;
					}
					closed = true;
					if (input.parentNode) {
						input.parentNode.removeChild(input);
					}
				};
				input.addEventListener('change', function() {
					onPick(input.value || '');
					close();
				});
				input.addEventListener('blur', function() {
					setTimeout(close, 300);
				});
				if (typeof input.showPicker === 'function') {
					try {
						input.showPicker();
					} catch (e) {
						input.focus();
						input.click();
					}
				} else {
					input.focus();
					input.click();
				}
				// #endif
			},
			pickStartDate() {
				this.openNativePicker('date', this.startDate ? '2020-' + this.startDate : '', (v) => {
					this.startDate = v ? v.substring(5) : '';
				});
			},
			pickStartTime() {
				this.openNativePicker('time', this.startTime, (v) => {
					this.startTime = v;
				});
			},
			pickEndDate() {
				this.openNativePicker('date', this.endDate ? '2020-' + this.endDate : '', (v) => {
					this.endDate = v ? v.substring(5) : '';
				});
			},
			pickEndTime() {
				this.openNativePicker('time', this.endTime, (v) => {
					this.endTime = v;
				});
			},
			pickOneCheckDate() {
				this.openNativePicker('date', this.oneCheckDate ? '2020-' + this.oneCheckDate : '', (v) => {
					this.oneCheckDate = v ? v.substring(5) : '';
				});
			},
			pickOneCheckTime() {
				this.openNativePicker('time', this.oneCheckTime, (v) => {
					this.oneCheckTime = v;
				});
			},

			//选择附件单击
			clickFile: function() {
				// #ifdef H5
				var input = document.createElement('input');
				input.type = 'file';
				input.accept = 'image/*';
				input.style.display = 'none';
				input.onchange = function(ev) {
					var file = ev.target.files && ev.target.files[0];
					if (input.parentNode) {
						input.parentNode.removeChild(input);
					}
					if (file) {
						this.compressImage(file);
					}
				}.bind(this);
				document.body.appendChild(input);
				input.click();
				// #endif
				// #ifndef H5
				uni.chooseImage({
					count: 1,
					sourceType: ['album'],
					success: (e) => {
						this.imgFile = e.tempFilePaths[0];
						this.toast('附件设置成功', 'success');
					}
				});
				// #endif
			},

			// 压缩成 base64，保证存到本地后还能显示
			compressImage: function(file) {
				var reader = new FileReader();
				reader.onload = () => {
					var img = new Image();
					img.onload = () => {
						var max = 1200;
						var w = img.width;
						var h = img.height;
						if (w > max || h > max) {
							var r = Math.min(max / w, max / h);
							w = Math.round(w * r);
							h = Math.round(h * r);
						}
						var canvas = document.createElement('canvas');
						canvas.width = w;
						canvas.height = h;
						canvas.getContext('2d').drawImage(img, 0, 0, w, h);
						this.imgFile = canvas.toDataURL('image/jpeg', 0.82);
						this.toast('附件设置成功', 'success');
					};
					img.onerror = () => this.toast('图片读取失败', 'none');
					img.src = reader.result;
				};
				reader.onerror = () => this.toast('图片读取失败', 'none');
				reader.readAsDataURL(file);
			},
			
			toast: function(str, icon) {
				var that = this;
				clearTimeout(this._snackTimer);
				this.snack = { show: true, text: str };
				this._snackTimer = setTimeout(function() {
					that.snack.show = false;
				}, 2200);
			}
		}
	}
</script>

<style>
	.m3-form {
		padding: 0 16px 24px;
	}

	.m3-appbar {
		height: 56px;
		display: flex;
		align-items: center;
		margin: 0 -16px;
		padding: 0 4px;
		background-color: var(--md-surface, #fafdf8);
	}

	.m3-appbar-lead {
		width: 48px;
		height: 48px;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 50%;
	}

	.m3-appbar-lead:active {
		background-color: var(--md-surface-container-high, #e6ebe4);
	}

	.m3-back {
		font-size: 30px;
		line-height: 30px;
		color: var(--md-on-surface, #171d19);
	}

	.m3-appbar-title {
		flex: 1;
		font-size: 18px;
		font-weight: 600;
		color: var(--md-on-surface, #171d19);
	}

	.m3-section {
		margin-top: 12px;
		padding: 18px 16px 8px;
		border-radius: 28px;
		background-color: var(--md-surface-container-low, #f2f6f0);
	}

	.m3-section-title {
		display: block;
		margin-bottom: 12px;
		font-size: 12px;
		font-weight: 700;
		letter-spacing: .8px;
		color: var(--md-primary, #166b4e);
	}

	.m3-field {
		margin-bottom: 14px;
	}

	.m3-label {
		display: block;
		margin-bottom: 6px;
		font-size: 12px;
		font-weight: 600;
		color: var(--md-on-surface-variant, #404943);
	}

	.m3-input {
		box-sizing: border-box;
		width: 100%;
		height: 52px;
		padding: 0 14px;
		border: none;
		border-radius: 12px 12px 4px 4px;
		background-color: var(--md-surface-container-high, #e6ebe4);
		font-size: 16px;
		color: var(--md-on-surface, #171d19);
	}

	.m3-row {
		display: flex;
		gap: 10px;
	}

	.m3-pick {
		flex: 1;
		height: 52px;
		padding: 0 14px;
		display: flex;
		align-items: center;
		border-radius: 12px 12px 4px 4px;
		background-color: var(--md-surface-container-high, #e6ebe4);
		font-size: 16px;
	}

	.m3-pick-ph {
		color: var(--md-outline, #707972);
	}

	.m3-pick-val {
		color: var(--md-on-surface, #171d19);
	}

	.m3-readonly {
		height: 52px;
		padding: 0 14px;
		display: flex;
		align-items: center;
		border-radius: 12px 12px 4px 4px;
		background-color: var(--md-surface-container, #ecf1ea);
	}

	.m3-readonly-ph {
		font-size: 15px;
		color: var(--md-outline, #707972);
	}

	.m3-readonly-val {
		font-size: 17px;
		font-weight: 600;
		color: var(--md-primary, #166b4e);
	}

	.m3-prefix-row {
		display: flex;
		align-items: center;
		height: 52px;
		border-radius: 12px 12px 4px 4px;
		background-color: var(--md-surface-container-high, #e6ebe4);
		overflow: hidden;
	}

	.m3-prefix {
		padding-left: 14px;
		font-size: 16px;
		color: var(--md-on-surface-variant, #404943);
	}

	.m3-input-inline {
		flex: 1;
		background-color: transparent;
	}

	.m3-file {
		margin-bottom: 14px;
	}

	.m3-file-empty {
		height: 104px;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		border-radius: 16px;
		border: 1.5px dashed var(--md-outline-variant, #c0c9c1);
		background-color: var(--md-surface-container-high, #e6ebe4);
	}

	.m3-file-plus {
		font-size: 24px;
		line-height: 26px;
		color: var(--md-primary, #166b4e);
	}

	.m3-file-hint {
		margin-top: 2px;
		font-size: 12px;
		color: var(--md-on-surface-variant, #404943);
	}

	.m3-file-img {
		width: 100%;
		height: 170px;
		border-radius: 16px;
	}

	.m3-submit-wrap {
		margin-top: 20px;
	}

	.m3-submit {
		height: 56px;
		border-radius: 28px;
		background-color: var(--md-primary, #166b4e);
		color: var(--md-on-primary, #ffffff);
		font-size: 16px;
		font-weight: 600;
		letter-spacing: .5px;
		display: flex;
		align-items: center;
		justify-content: center;
		transition: transform .16s cubic-bezier(.2, 0, 0, 1);
	}

	.m3-submit:active {
		transform: scale(.97);
	}

	/* ===== MD3 Snackbar ===== */
	.m3-snackbar {
		position: fixed;
		left: 16px;
		right: 16px;
		bottom: calc(24px + env(safe-area-inset-bottom));
		z-index: 9600;
		padding: 14px 16px;
		border-radius: 8px;
		background-color: #2E332E;
		color: #F0F4EF;
		font-size: 14px;
		line-height: 20px;
		box-shadow: 0 4px 12px rgba(0, 0, 0, .2);
		pointer-events: none;
		animation: m3-snack-in .2s cubic-bezier(.2, 0, 0, 1);
	}

	@keyframes m3-snack-in {
		from { opacity: 0; transform: translateY(12px); }
		to { opacity: 1; transform: translateY(0); }
	}

	/* ===== MD3 选择器弹层 ===== */
	.m3-scrim {
		position: fixed;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, .32);
		z-index: 9000;
	}

	.m3-sheet {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 9001;
		padding: 10px 0 calc(12px + env(safe-area-inset-bottom));
		border-radius: 28px 28px 0 0;
		background-color: var(--md-surface-container-high, #e6ebe4);
		box-shadow: 0 -8px 24px rgba(0, 0, 0, .16);
		animation: m3-sheet-in .22s cubic-bezier(.2, 0, 0, 1);
	}

	@keyframes m3-sheet-in {
		from { transform: translateY(100%); }
		to { transform: translateY(0); }
	}

	.m3-sheet-grip {
		width: 32px;
		height: 4px;
		margin: 0 auto 6px;
		border-radius: 2px;
		background-color: var(--md-outline-variant, #c0c9c1);
	}

	.m3-sheet-head {
		height: 52px;
		display: flex;
		align-items: center;
		padding: 0 8px;
	}

	.m3-sheet-title {
		flex: 1;
		text-align: center;
		font-size: 16px;
		font-weight: 600;
		color: var(--md-on-surface, #171d19);
	}

	.m3-sheet-btn {
		min-width: 64px;
		height: 40px;
		line-height: 40px;
		text-align: center;
		border-radius: 20px;
		font-size: 14px;
		font-weight: 600;
		color: var(--md-on-surface-variant, #404943);
	}

	.m3-sheet-btn:active {
		background-color: var(--md-surface-container, #ecf1ea);
	}

	.m3-sheet-btn.is-primary {
		color: var(--md-primary, #166b4e);
	}

	.m3-wheel {
		display: flex;
		padding: 0 12px;
	}

	.m3-wheel-col {
		flex: 1;
		height: 220px;
		overscroll-behavior: contain;
	}

	/* 阻断滚轮到底后把滚动传给页面（scroll chaining） */
	.m3-wheel-col .uni-scroll-view {
		overscroll-behavior: contain;
		-webkit-overflow-scrolling: touch;
	}

	.m3-sheet {
		overscroll-behavior: contain;
	}

	.m3-wheel-pad {
		height: 88px;
	}

	.m3-wheel-item {
		height: 44px;
		line-height: 44px;
		text-align: center;
		font-size: 17px;
		color: var(--md-on-surface-variant, #404943);
		border-radius: 22px;
	}

	.m3-wheel-item.is-on {
		background-color: var(--md-primary-container, #a6f2ce);
		color: var(--md-on-primary-container, #002116);
		font-weight: 600;
	}
</style>

